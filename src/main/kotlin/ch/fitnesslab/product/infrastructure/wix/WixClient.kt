package ch.fitnesslab.product.infrastructure.wix

import ch.fitnesslab.product.infrastructure.wix.v3.WixPlan
import ch.fitnesslab.product.infrastructure.wix.v3.WixQueryPlansResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

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
}

