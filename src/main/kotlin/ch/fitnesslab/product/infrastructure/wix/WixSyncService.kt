package ch.fitnesslab.product.infrastructure.wix

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.generated.model.ProductView
import ch.fitnesslab.product.application.FindAllProductsQuery
import ch.fitnesslab.product.application.ProductUpdatedUpdate
import ch.fitnesslab.product.domain.*
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.infrastructure.ProductRepository
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
    private val productRepository: ProductRepository
) {
    private val logger = LoggerFactory.getLogger(WixSyncService::class.java)

    fun syncWixProducts() {
        try {
            val wixPlans = wixClient.fetchPricingPlans()
            logger.info("Fetched ${wixPlans.size} Wix pricing plans")

            wixPlans.forEach { wixPlan ->
                syncWixPlan(wixPlan)
            }

            logger.info("Wix sync completed successfully")
        } catch (e: Exception) {
            logger.error("Wix sync failed: ${e.message}", e)
        }
    }

    private fun syncWixPlan(wixPlan: WixPlan) {
        try {
            // Skip if no ID
            if (wixPlan.id.isNullOrBlank()) {
                logger.warn("Skipping Wix plan without ID: ${wixPlan.name}")
                return
            }

            // Check if product with this Wix ID already exists
            val existingProduct = productRepository.findAll()
                .firstOrNull { product ->
                    product.linkedPlatforms?.any {
                        it.platformName == "wix" && it.idOnPlatform == wixPlan.id
                    } == true
                }

            if (existingProduct != null) {
                logger.debug("Product with Wix ID ${wixPlan.id} already exists, skipping")
                return
            }

            val subscriptionQuery = queryGateway.subscriptionQuery(
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
        } catch (e: Exception) {
            logger.error("Failed to sync Wix plan ${wixPlan.id}: ${e.message}", e)
        }
    }

    private fun mapWixPlanToCommand(wixPlan: WixPlan): CreateProductCommand {
        val linkedPlatformSync = LinkedPlatformSync(
            platformName = "wix",
            idOnPlatform = wixPlan.id,
            revision = wixPlan.revision,
            visibilityOnPlatform = mapWixVisibility(wixPlan.visibility),
            isSynced = true,
            isSourceOfTruth = false,
            lastSyncedAt = Instant.now(),
            syncError = null
        )

        // Get first pricing variant
        val pricingVariant = wixPlan.pricingVariants.firstOrNull()

        return CreateProductCommand(
            productId = ProductVariantId(UUID.randomUUID()),
            slug = wixPlan.slug ?: generateSlug(wixPlan.name ?: "unnamed-plan"),
            name = wixPlan.name.toString(),
            productType = "Wix Plan",
            audience = ProductAudience.BOTH,
            requiresMembership = false,
            pricingVariant = mapWixPricingVariant(pricingVariant),
            behavior = ProductBehaviorConfig(
                canBePaused = false,
                renewalLeadTimeDays = null,
                maxActivePerCustomer = null,
                maxPurchasesPerBuyer = wixPlan.maxPurchasesPerBuyer,
                numberOfSessions = null
            ),
            description = wixPlan.description,
            termsAndConditions = null, // Not available in v3
            visibility = mapWixVisibilityToProductVisibility(wixPlan.visibility),
            buyable = wixPlan.buyable == true,
            buyerCanCancel = wixPlan.buyerCanCancel == true,
            perks = wixPlan.perks
                .mapNotNull { it.description }
                .takeIf { it.isNotEmpty() },
            linkedPlatforms = listOf(linkedPlatformSync)
        )
    }

    private fun mapWixVisibility(visibility: String?): PlatformVisibility? {
        return when (visibility?.uppercase()) {
            "PUBLIC" -> PlatformVisibility.PUBLISHED
            "HIDDEN" -> PlatformVisibility.HIDDEN
            "ARCHIVED" -> PlatformVisibility.ARCHIVED
            else -> PlatformVisibility.NOT_PUBLISHED
        }
    }

    private fun mapWixVisibilityToProductVisibility(visibility: String?): ProductVisibility {
        return when (visibility?.uppercase()) {
            "PUBLIC" -> ProductVisibility.PUBLIC
            "HIDDEN" -> ProductVisibility.HIDDEN
            "ARCHIVED" -> ProductVisibility.ARCHIVED
            else -> ProductVisibility.HIDDEN
        }
    }

    private fun mapWixPricingVariant(pricingVariant: WixPricingVariantV3?): PricingVariantConfig {
        if (pricingVariant == null) {
            return createDefaultPricing()
        }

        // Extract flat rate from first pricing strategy
        val flatRate = pricingVariant.pricingStrategies
            .firstOrNull()
            ?.flatRate
            ?.amount
            ?.toBigDecimalOrNull() ?: BigDecimal.ZERO

        // Determine pricing model from billing terms
        val pricingModel = determinePricingModel(pricingVariant.billingTerms)

        // Map billing cycle
        val billingCycle = pricingVariant.billingTerms?.billingCycle?.let { cycle ->
            PricingDuration(
                interval = mapWixPeriodToInterval(cycle.period),
                count = cycle.count ?: 1
            )
        }

        // Map duration (for fixed-duration plans)
        val duration = if (pricingVariant.billingTerms?.endType == "CYCLES_COMPLETED") {
            val cycleCount = pricingVariant.billingTerms?.cyclesCompletedDetails?.billingCycleCount ?: 1
            val period = pricingVariant.billingTerms?.billingCycle?.period
            val count = (pricingVariant.billingTerms?.billingCycle?.count ?: 1) * cycleCount
            PricingDuration(
                interval = mapWixPeriodToInterval(period),
                count = count
            )
        } else null

        // Map free trial
        val freeTrial = if ((pricingVariant.freeTrialDays ?: 0) > 0) {
            PricingDuration(
                interval = BillingInterval.DAY,
                count = pricingVariant.freeTrialDays!!
            )
        } else null

        return PricingVariantConfig(
            pricingModel = pricingModel,
            flatRate = flatRate,
            billingCycle = billingCycle,
            duration = duration,
            freeTrial = freeTrial
        )
    }

    private fun determinePricingModel(billingTerms: ch.fitnesslab.product.infrastructure.wix.v3.WixBillingTerms?): PricingModel {
        return when (billingTerms?.endType?.uppercase()) {
            "UNTIL_CANCELLED" -> PricingModel.SUBSCRIPTION
            "CYCLES_COMPLETED" -> PricingModel.SINGLE_PAYMENT_FOR_DURATION
            else -> PricingModel.SUBSCRIPTION
        }
    }

    private fun mapWixPeriodToInterval(period: String?): BillingInterval {
        return when (period?.uppercase()) {
            "DAY" -> BillingInterval.DAY
            "WEEK" -> BillingInterval.WEEK
            "MONTH" -> BillingInterval.MONTH
            "YEAR" -> BillingInterval.YEAR
            else -> BillingInterval.MONTH
        }
    }

    private fun createDefaultPricing(): PricingVariantConfig {
        return PricingVariantConfig(
            pricingModel = PricingModel.SUBSCRIPTION,
            flatRate = BigDecimal.ZERO,
            billingCycle = PricingDuration(BillingInterval.MONTH, 1),
            duration = null,
            freeTrial = null
        )
    }

    private fun generateSlug(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
    }
}
