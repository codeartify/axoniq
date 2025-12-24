package ch.fitnesslab.product.infrastructure.wix

import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.generated.model.ProductView
import ch.fitnesslab.product.application.FindAllProductsQuery
import ch.fitnesslab.product.application.ProductUpdatedUpdate
import ch.fitnesslab.product.domain.*
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.infrastructure.ProductRepository
import ch.fitnesslab.product.infrastructure.ProductVariantEntity
import ch.fitnesslab.product.infrastructure.wix.v3.WixBillingTerms
import ch.fitnesslab.product.infrastructure.wix.v3.WixPlan
import ch.fitnesslab.product.infrastructure.wix.v3.WixPricingVariantV3
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Service
class WixSyncService(
    private val wixClient: WixClient,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val productRepository: ProductRepository,
) {
    private val logger = LoggerFactory.getLogger(WixSyncService::class.java)

    fun uploadProduct(productId: String) {
        this.productRepository.findById(UUID.fromString(productId)).let { product -> }
    }

    fun syncWixProducts() {
        try {
            val wixPlans = fetchWixPlans()

            createProductsFrom(wixPlans)
        } catch (e: Exception) {
            logger.error("Wix sync failed: ${e.message}", e)
        }
    }

    private fun fetchWixPlans(): List<WixPlan> {
        val wixPlans = wixClient.fetchPricingPlans()
        logger.info("Fetched ${wixPlans.size} Wix pricing plans")
        return wixPlans
    }

    private fun createProductsFrom(wixPlans: List<WixPlan>) {
        wixPlans.forEach { wixPlan ->
            syncWixPlan(wixPlan)
        }
        logger.info("Wix sync completed successfully")
    }

    private fun syncWixPlan(wixPlan: WixPlan) {
        val wixPlanId = wixPlan.id

        try {
            if (wixPlanId.isNullOrBlank()) {
                logger.warn("Skipping Wix plan without ID: ${wixPlan.name}")
                return
            }

            if (productWithWixIdExistsAlready(wixPlanId)) {
                logger.debug("Product with Wix ID $wixPlanId already exists, skipping")
                return
            }

            createProductFromWixPlan(wixPlan)
        } catch (e: Exception) {
            logger.error("Failed to sync Wix plan $wixPlanId: ${e.message}", e)
        }
    }

    private fun createProductFromWixPlan(wixPlan: WixPlan) {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllProductsQuery(),
                ResponseTypes.multipleInstancesOf(ProductView::class.java),
                ResponseTypes.instanceOf(ProductUpdatedUpdate::class.java),
            )

        try {
            val command = mapWixPlanToCommand(wixPlan)

            commandGateway.sendAndWait<Unit>(command)

            waitForUpdateOf(subscriptionQuery)

            logger.info("Created product from Wix plan: ${wixPlan.name} (${wixPlan.id})")
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun productWithWixIdExistsAlready(id: String): Boolean {
        return productRepository
            .findAll()
            .firstOrNull { product -> productExistsAlready(product, id) } != null
    }

    private fun productExistsAlready(
        product: ProductVariantEntity?,
        id: String
    ): Boolean = product?.linkedPlatforms?.any {
        it.platformName == "wix" && it.idOnPlatform == id
    } == true

    private fun mapWixPlanToCommand(wixPlan: WixPlan): CreateProductCommand {
        val linkedPlatformSync =
            LinkedPlatformSync(
                platformName = "wix",
                idOnPlatform = wixPlan.id,
                revision = wixPlan.revision,
                visibilityOnPlatform = mapWixVisibility(wixPlan.visibility),
                isSynced = true,
                isSourceOfTruth = false,
                lastSyncedAt = Instant.now(),
                syncError = null,
            )

        val pricingVariant = wixPlan.pricingVariants.firstOrNull()

        return CreateProductCommand(
            productId = ProductVariantId(UUID.randomUUID()),
            slug = wixPlan.slug ?: generateSlug(wixPlan.name ?: "unnamed-plan"),
            name = wixPlan.name.toString(),
            productType = "Wix Plan",
            audience = ProductAudience.BOTH,
            requiresMembership = false,
            pricingVariant = mapWixPricingVariant(pricingVariant),
            behavior =
                ProductBehaviorConfig(
                    canBePaused = false,
                    renewalLeadTimeDays = null,
                    maxActivePerCustomer = null,
                    maxPurchasesPerBuyer = wixPlan.maxPurchasesPerBuyer,
                    numberOfSessions = null,
                ),
            description = wixPlan.description,
            termsAndConditions = null, // Not available in v3
            visibility = mapWixVisibilityToProductVisibility(wixPlan.visibility),
            buyable = wixPlan.buyable == true,
            buyerCanCancel = wixPlan.buyerCanCancel == true,
            perks =
                wixPlan.perks
                    .mapNotNull { it.description }
                    .takeIf { it.isNotEmpty() },
            linkedPlatforms = listOf(linkedPlatformSync),
        )
    }

    private fun mapWixVisibility(visibility: String?): PlatformVisibility? =
        when (visibility?.uppercase()) {
            "PUBLIC" -> PlatformVisibility.PUBLISHED
            "HIDDEN" -> PlatformVisibility.HIDDEN
            "ARCHIVED" -> PlatformVisibility.ARCHIVED
            else -> PlatformVisibility.NOT_PUBLISHED
        }

    private fun mapWixVisibilityToProductVisibility(visibility: String?): ProductVisibility =
        when (visibility?.uppercase()) {
            "PUBLIC" -> ProductVisibility.PUBLIC
            "HIDDEN" -> ProductVisibility.HIDDEN
            "ARCHIVED" -> ProductVisibility.ARCHIVED
            else -> ProductVisibility.HIDDEN
        }

    private fun mapWixPricingVariant(pricingVariant: WixPricingVariantV3?): PricingVariantConfig {
        if (pricingVariant == null) {
            return createDefaultPricing()
        }

        return PricingVariantConfig(
            pricingModel = pricingModelFrom(pricingVariant.billingTerms),
            flatRate = flatRateFrom(pricingVariant),
            billingCycle = billingCycleFrom(pricingVariant),
            duration = durationFrom(pricingVariant),
            freeTrial = freeTrialFrom(pricingVariant),
        )
    }

    private fun freeTrialFrom(pricingVariant: WixPricingVariantV3): PricingDuration? = when {
        hasFreeTrialPeriod(pricingVariant) -> PricingDuration(
            interval = BillingInterval.DAY,
            count = pricingVariant.freeTrialDays!!,
        )

        else -> null
    }

    private fun hasFreeTrialPeriod(pricingVariant: WixPricingVariantV3): Boolean =
        (pricingVariant.freeTrialDays ?: 0) > 0

    private fun durationFrom(pricingVariant: WixPricingVariantV3): PricingDuration? = when {
        isCyclic(pricingVariant) -> PricingDuration(
            interval = mapWixPeriodToInterval(pricingVariant.billingTerms?.billingCycle?.period),
            count = countFrom(pricingVariant.billingTerms),
        )

        else -> null
    }

    private fun isCyclic(pricingVariant: WixPricingVariantV3): Boolean =
        pricingVariant.billingTerms?.endType == "CYCLES_COMPLETED"

    private fun countFrom(billingTerms: WixBillingTerms?): Int =
        (billingTerms?.billingCycle?.count ?: 1) * cycleCountFrom(billingTerms)

    private fun cycleCountFrom(billingTerms: WixBillingTerms?): Int =
        billingTerms?.cyclesCompletedDetails?.billingCycleCount ?: 1

    private fun billingCycleFrom(pricingVariant: WixPricingVariantV3): PricingDuration? =
        pricingVariant.billingTerms?.billingCycle?.let { cycle ->
            PricingDuration(
                interval = mapWixPeriodToInterval(cycle.period),
                count = cycle.count ?: 1,
            )
        }

    private fun flatRateFrom(pricingVariant: WixPricingVariantV3): BigDecimal = pricingVariant.pricingStrategies
        .firstOrNull()
        ?.flatRate
        ?.amount
        ?.toBigDecimalOrNull() ?: BigDecimal.ZERO

    private fun pricingModelFrom(billingTerms: WixBillingTerms?): PricingModel =
        when (billingTerms?.endType?.uppercase()) {
            "UNTIL_CANCELLED" -> PricingModel.SUBSCRIPTION
            "CYCLES_COMPLETED" -> PricingModel.SINGLE_PAYMENT_FOR_DURATION
            else -> PricingModel.SUBSCRIPTION
        }

    private fun mapWixPeriodToInterval(period: String?): BillingInterval =
        when (period?.uppercase()) {
            "DAY" -> BillingInterval.DAY
            "WEEK" -> BillingInterval.WEEK
            "MONTH" -> BillingInterval.MONTH
            "YEAR" -> BillingInterval.YEAR
            else -> BillingInterval.MONTH
        }

    private fun createDefaultPricing(): PricingVariantConfig =
        PricingVariantConfig(
            pricingModel = PricingModel.SUBSCRIPTION,
            flatRate = BigDecimal.ZERO,
            billingCycle = PricingDuration(BillingInterval.MONTH, 1),
            duration = null,
            freeTrial = null,
        )

    private fun generateSlug(name: String): String =
        name
            .lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
}
