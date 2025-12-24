package ch.fitnesslab.product.infrastructure.wix

import ch.fitnesslab.product.domain.BillingInterval
import ch.fitnesslab.product.domain.PricingModel
import ch.fitnesslab.product.domain.ProductVisibility
import ch.fitnesslab.product.infrastructure.ProductVariantEntity
import ch.fitnesslab.product.infrastructure.wix.v3.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class WixClient(
    @Value("\${wix.token}") private val wixToken: String,
    @Value("\${wix.site.id:}") private val wixSiteId: String,
    @Value("\${wix.api.url:}") private val wixApiUrl: String,
    private val restTemplate: RestTemplate = RestTemplate(),
) {
    private val logger = LoggerFactory.getLogger(WixClient::class.java)

    fun fetchPricingPlans(): List<WixPlan> {
        if (wixToken.isBlank() || wixSiteId.isBlank()) {
            logger.warn("Wix credentials not configured. Skipping Wix sync.")
            return emptyList()
        }

        return try {
            val response = fetchAllWixPlans()
            logger.info("Successfully fetched ${response.body?.plans?.size ?: 0} pricing plans from Wix")
            response.body?.plans ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch pricing plans from Wix: ${e.message}", e)
            throw WixSyncException("Failed to fetch pricing plans from Wix", e)
        }
    }

    private fun fetchAllWixPlans(): ResponseEntity<WixQueryPlansResponse?> {
        val body = queryBody()
        val headers = wixHeaders()
        val requestEntity = HttpEntity(body, headers)
        return restTemplate.exchange(wixApiUrl, HttpMethod.POST, requestEntity, WixQueryPlansResponse::class.java)
    }

    private fun queryBody(): String = """
                    {
                      "query": {
                        "cursorPaging": {
                          "limit": 100
                        }
                      }
                    }
                    """.trimIndent()

    private fun wixHeaders(): HttpHeaders = HttpHeaders().apply {
        set("Authorization", wixToken)
        set("wix-site-id", wixSiteId)
        set("Content-Type", "application/json")
    }

    fun uploadPricingPlanToWix(product: ProductVariantEntity): WixPlan? {
        if (wixToken.isBlank() || wixSiteId.isBlank()) {
            logger.warn("Wix credentials not configured. Skipping Wix upload.")
            return null
        }

        try {
            val wixPlan = mapProductToWixPlan(product)
            val response = sendCreatePlanRequest(wixPlan)

            logger.info("Successfully uploaded pricing plan to Wix: ${response.body?.plan?.id}")
            return response.body?.plan
        } catch (e: Exception) {
            logger.error("Failed to upload pricing plan to Wix: ${e.message}", e)
            throw WixSyncException("Failed to upload pricing plan to Wix", e)
        }
    }

    private fun mapProductToWixPlan(product: ProductVariantEntity): WixPlan {
        val pricingVariant = mapProductToPricingVariant(product)

        val wixPerks = product.perks?.map { perkDescription ->
            WixPerk(
                id = null,
                description = perkDescription
            )
        } ?: emptyList()

        return WixPlan(
            id = null,  // New plan, no ID yet
            revision = null,
            createdDate = null,
            updatedDate = null,
            name = product.name,
            slug = product.slug,
            description = product.description,
            maxPurchasesPerBuyer = product.maxPurchasesPerBuyer,
            pricingVariants = listOf(pricingVariant),
            perks = wixPerks,
            visibility = mapProductVisibilityToWix(product.visibility),
            buyable = product.buyable,
            status = "ACTIVE",
            buyerCanCancel = product.buyerCanCancel,
            archived = false,
            primary = false,
            currency = "EUR"  // Default, consider making configurable
        )
    }

    private fun mapProductToPricingVariant(product: ProductVariantEntity): WixPricingVariantV3 {
        val billingTerms = buildBillingTerms(product)
        val pricingStrategies = listOf(
            WixPricingStrategy(
                flatRate = WixFlatRate(
                    amount = product.flatRate.toPlainString()
                )
            )
        )
        
        val freeTrialDays = product.freeTrialInterval?.let {
            when (it) {
                BillingInterval.DAY -> product.freeTrialCount ?: 0
                BillingInterval.WEEK -> (product.freeTrialCount ?: 0) * 7
                BillingInterval.MONTH -> (product.freeTrialCount ?: 0) * 30
                BillingInterval.YEAR -> (product.freeTrialCount ?: 0) * 365
            }
        }

        return WixPricingVariantV3(
            id = UUID.randomUUID().toString(),
            name = product.name,
            freeTrialDays = freeTrialDays,
            fees = emptyList(),
            billingTerms = billingTerms,
            pricingStrategies = pricingStrategies
        )
    }

    private fun buildBillingTerms(product: ProductVariantEntity): WixBillingTerms? {
        // Only create billing terms for subscription-based products
        if (product.pricingModel != PricingModel.SUBSCRIPTION) {
            return null
        }

        val billingCycle = product.billingCycleInterval?.let { interval ->
            WixBillingCycle(
                period = mapBillingIntervalToWixPeriod(interval),
                count = product.billingCycleCount ?: 1
            )
        }

        val cyclesCompletedDetails = product.durationInterval?.let { _ ->
            WixCyclesCompletedDetails(
                billingCycleCount = product.durationCount
            )
        }

        val endType = if (product.durationCount != null) {
            "CYCLES_COMPLETED"
        } else {
            "UNTIL_CANCELLED"
        }

        return WixBillingTerms(
            billingCycle = billingCycle,
            startType = "ON_PURCHASE",
            endType = endType,
            cyclesCompletedDetails = cyclesCompletedDetails
        )
    }

    private fun mapBillingIntervalToWixPeriod(interval: BillingInterval): String {
        return when (interval) {
            BillingInterval.DAY -> "DAY"
            BillingInterval.WEEK -> "WEEK"
            BillingInterval.MONTH -> "MONTH"
            BillingInterval.YEAR -> "YEAR"
        }
    }

    private fun mapProductVisibilityToWix(visibility: ProductVisibility): String {
        return when (visibility) {
            ProductVisibility.PUBLIC -> "PUBLIC"
            ProductVisibility.HIDDEN -> "HIDDEN"
            ProductVisibility.ARCHIVED -> "ARCHIVED"
        }
    }

    private fun sendCreatePlanRequest(wixPlan: WixPlan): ResponseEntity<WixCreatePlanResponse?> {
        val headers = wixHeaders().apply {
            set("Content-Type", "application/json")
        }

        val body = mapOf("plan" to wixPlan)
        val requestEntity = HttpEntity(body, headers)

        val createPlanUrl = wixApiUrl.replace("/query", "")

        return restTemplate.exchange(
            createPlanUrl,
            HttpMethod.POST,
            requestEntity,
            WixCreatePlanResponse::class.java
        )
    }
}
