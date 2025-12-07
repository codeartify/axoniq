package ch.fitnesslab.product.infrastructure.wix

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WixClient(
    @Value("\${wix.token:}") private val wixToken: String,
    @Value("\${wix.site.id:}") private val wixSiteId: String,
    private val restTemplate: RestTemplate = RestTemplate()
) {
    private val logger = LoggerFactory.getLogger(WixClient::class.java)
    private val wixApiUrl = "https://www.wixapis.com/pricing-plans/v3/plans"

    fun fetchPricingPlans(): List<WixPlan> {
        if (wixToken.isBlank() || wixSiteId.isBlank()) {
            logger.warn("Wix credentials not configured. Skipping Wix sync.")
            return emptyList()
        }

        return try {
            val headers = HttpHeaders().apply {
                set("Authorization", wixToken)
                set("wix-site-id", wixSiteId)
                set("Content-Type", "application/json")
            }

            val request = HttpEntity<String>(headers)
            val response = restTemplate.exchange(
                wixApiUrl,
                HttpMethod.GET,
                request,
                WixPricingPlansResponse::class.java
            )

            logger.info("Successfully fetched ${response.body?.plans?.size ?: 0} pricing plans from Wix")
            response.body?.plans ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch pricing plans from Wix: ${e.message}", e)
            throw WixSyncException("Failed to fetch pricing plans from Wix", e)
        }
    }
}

class WixSyncException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
