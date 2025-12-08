package ch.fitnesslab.billing.infrastructure.bexio

import ch.fitnesslab.customers.infrastructure.bexio.BexioContactDto
import ch.fitnesslab.customers.infrastructure.bexio.BexioCreateContactRequest
import ch.fitnesslab.customers.infrastructure.bexio.BexioUpdateContactRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class BexioClient(
    @Value("\${bexio.api.token}") private val bexioToken: String,
    private val restTemplate: RestTemplate = RestTemplate()
) {
    private val logger = LoggerFactory.getLogger(BexioClient::class.java)
    private val bexioApiUrl = "https://api.bexio.com/2.0"

    fun fetchInvoices(): List<BexioInvoiceDto> {
        if (bexioToken.isBlank()) {
            logger.warn("Bexio API token not configured. Skipping invoice fetch.")
            return emptyList()
        }

        return try {
            val headers = createHeaders()
            val request = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                "$bexioApiUrl/kb_invoice",
                HttpMethod.GET,
                request,
                Array<BexioInvoiceDto>::class.java
            )

            logger.info("Successfully fetched ${response.body?.size ?: 0} invoices from Bexio")
            response.body?.toList() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch invoices from Bexio: ${e.message}", e)
            throw BexioException("Failed to fetch invoices from Bexio", e)
        }
    }

    fun fetchInvoicesByContactId(contactId: Int): List<BexioInvoiceDto> {
        if (bexioToken.isBlank()) {
            logger.warn("Bexio API token not configured. Skipping invoice fetch.")
            return emptyList()
        }

        return try {
            val headers = createHeaders()
            val request = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                "$bexioApiUrl/kb_invoice?contact_id=$contactId",
                HttpMethod.GET,
                request,
                Array<BexioInvoiceDto>::class.java
            )

            logger.info("Successfully fetched ${response.body?.size ?: 0} invoices for contact $contactId from Bexio")
            response.body?.toList() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch invoices from Bexio: ${e.message}", e)
            throw BexioException("Failed to fetch invoices from Bexio", e)
        }
    }

    fun fetchInvoiceById(invoiceId: Int): BexioInvoiceDto? {
        if (bexioToken.isBlank()) {
            logger.warn("Bexio API token not configured. Skipping invoice fetch.")
            return null
        }

        return try {
            val headers = createHeaders()
            val request = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                "$bexioApiUrl/kb_invoice/$invoiceId",
                HttpMethod.GET,
                request,
                BexioInvoiceDto::class.java
            )

            logger.info("Successfully fetched invoice $invoiceId from Bexio")
            response.body
        } catch (e: Exception) {
            logger.error("Failed to fetch invoice $invoiceId from Bexio: ${e.message}", e)
            throw BexioException("Failed to fetch invoice from Bexio", e)
        }
    }

    fun createInvoice(request: BexioCreateInvoiceRequest): BexioInvoiceDto {
        if (bexioToken.isBlank()) {
            throw BexioException("Bexio API token not configured")
        }

        return try {
            val headers = createHeaders()
            val httpEntity = HttpEntity(request, headers)

            val response = restTemplate.exchange(
                "$bexioApiUrl/kb_invoice",
                HttpMethod.POST,
                httpEntity,
                BexioInvoiceDto::class.java
            )

            logger.info("Successfully created invoice in Bexio: ${response.body?.id}")
            response.body ?: throw BexioException("No response body from Bexio")
        } catch (e: Exception) {
            logger.error("Failed to create invoice in Bexio: ${e.message}", e)
            throw BexioException("Failed to create invoice in Bexio", e)
        }
    }

    // ========== Contact Methods ==========

    fun createContact(request: BexioCreateContactRequest): BexioContactDto {
        if (bexioToken.isBlank()) {
            throw BexioException("Bexio API token not configured")
        }

        return try {
            val headers = createHeaders()
            val httpEntity = HttpEntity(request, headers)

            val response = restTemplate.exchange(
                "$bexioApiUrl/contact",
                HttpMethod.POST,
                httpEntity,
                BexioContactDto::class.java
            )

            logger.info("Successfully created contact in Bexio: ${response.body?.id}")
            response.body ?: throw BexioException("No response body from Bexio")
        } catch (e: Exception) {
            logger.error("Failed to create contact in Bexio: ${e.message}", e)
            throw BexioException("Failed to create contact in Bexio", e)
        }
    }

    fun updateContact(contactId: Int, request: BexioUpdateContactRequest): BexioContactDto {
        if (bexioToken.isBlank()) {
            throw BexioException("Bexio API token not configured")
        }

        return try {
            val headers = createHeaders()
            val httpEntity = HttpEntity(request, headers)

            val response = restTemplate.exchange(
                "$bexioApiUrl/contact/$contactId",
                HttpMethod.POST,
                httpEntity,
                BexioContactDto::class.java
            )

            logger.info("Successfully updated contact in Bexio: $contactId")
            response.body ?: throw BexioException("No response body from Bexio")
        } catch (e: Exception) {
            logger.error("Failed to update contact $contactId in Bexio: ${e.message}", e)
            throw BexioException("Failed to update contact in Bexio", e)
        }
    }

    fun deleteContact(contactId: Int) {
        if (bexioToken.isBlank()) {
            throw BexioException("Bexio API token not configured")
        }

        try {
            val headers = createHeaders()
            val request = HttpEntity<String>(headers)

            restTemplate.exchange(
                "$bexioApiUrl/contact/$contactId",
                HttpMethod.DELETE,
                request,
                Void::class.java
            )

            logger.info("Successfully deleted contact in Bexio: $contactId")
        } catch (e: Exception) {
            logger.error("Failed to delete contact $contactId in Bexio: ${e.message}", e)
            throw BexioException("Failed to delete contact in Bexio", e)
        }
    }

    fun fetchContactById(contactId: Int): BexioContactDto? {
        if (bexioToken.isBlank()) {
            logger.warn("Bexio API token not configured. Skipping contact fetch.")
            return null
        }

        return try {
            val headers = createHeaders()
            val request = HttpEntity<String>(headers)

            val response = restTemplate.exchange(
                "$bexioApiUrl/contact/$contactId",
                HttpMethod.GET,
                request,
                BexioContactDto::class.java
            )

            logger.info("Successfully fetched contact $contactId from Bexio")
            response.body
        } catch (e: Exception) {
            logger.error("Failed to fetch contact $contactId from Bexio: ${e.message}", e)
            throw BexioException("Failed to fetch contact from Bexio", e)
        }
    }

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Bearer $bexioToken")
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
        }
    }
}

class BexioException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
