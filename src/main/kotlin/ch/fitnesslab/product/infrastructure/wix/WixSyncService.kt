package ch.fitnesslab.product.infrastructure.wix

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.*
import ch.fitnesslab.product.domain.commands.CreateProductCommand
import ch.fitnesslab.product.infrastructure.ProductRepository
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Service
class WixSyncService(
    private val wixClient: WixClient,
    private val commandGateway: CommandGateway,
    private val productRepository: ProductRepository
) {
    private val logger = LoggerFactory.getLogger(WixSyncService::class.java)

    fun syncWixProducts() {
        try {
            val wixPlans = wixClient.fetchPricingPlans()
            logger.info("Syncing ${wixPlans.size} Wix pricing plans")

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

            // Create new product from Wix plan
            val command = mapWixPlanToCommand(wixPlan)
            commandGateway.sendAndWait<Unit>(command)
            logger.info("Created product from Wix plan: ${wixPlan.name} (${wixPlan.id})")
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

        // Get first pricing variant (Wix API ensures minItems: 1, maxItems: 1)
        val pricingVariant = wixPlan.pricingVariants?.firstOrNull()

        return CreateProductCommand(
            productId = ProductVariantId(UUID.randomUUID()),
            slug = wixPlan.slug ?: generateSlug(wixPlan.name),
            name = wixPlan.name,
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
            termsAndConditions = wixPlan.termsAndConditions,
            visibility = mapWixVisibilityToProductVisibility(wixPlan.visibility),
            buyable = wixPlan.buyable,
            buyerCanCancel = wixPlan.buyerCanCancel,
            perks = wixPlan.perks?.map { it.title },
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

    private fun mapWixPricingVariant(pricingVariant: WixPricingVariant?): PricingVariantConfig {
        if (pricingVariant == null) {
            return createDefaultPricing()
        }

        val flatRate = pricingVariant.flatRate?.toBigDecimal() ?: BigDecimal.ZERO
        val pricingModel = when (pricingVariant.pricingModel?.uppercase()) {
            "SUBSCRIPTION" -> PricingModel.SUBSCRIPTION
            "SINGLE_PAYMENT_FOR_DURATION" -> PricingModel.SINGLE_PAYMENT_FOR_DURATION
            "SINGLE_PAYMENT_UNLIMITED" -> PricingModel.SINGLE_PAYMENT_UNLIMITED
            else -> PricingModel.SUBSCRIPTION
        }

        return PricingVariantConfig(
            pricingModel = pricingModel,
            flatRate = flatRate,
            billingCycle = mapWixDuration(pricingVariant.billingCycle),
            duration = mapWixDuration(pricingVariant.duration),
            freeTrial = mapWixDuration(pricingVariant.freeTrial)
        )
    }

    private fun mapWixDuration(wixDuration: WixDuration?): PricingDuration? {
        if (wixDuration == null || wixDuration.interval == null || wixDuration.count == null) {
            return null
        }

        val interval = when (wixDuration.interval.uppercase()) {
            "DAY" -> BillingInterval.DAY
            "WEEK" -> BillingInterval.WEEK
            "MONTH" -> BillingInterval.MONTH
            "YEAR" -> BillingInterval.YEAR
            else -> BillingInterval.MONTH
        }

        return PricingDuration(interval, wixDuration.count)
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
