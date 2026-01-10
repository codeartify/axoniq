package ch.fitnesslab.customers.infrastructure.bexio

import ch.fitnesslab.billing.infrastructure.bexio.BexioClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BexioContactService(
    private val bexioClient: BexioClient,
) {
    private val logger = LoggerFactory.getLogger(BexioContactService::class.java)

    fun createContact(request: BexioCreateContactRequest): BexioContactResponse {
        val bexioContact = bexioClient.createContact(request)
        logger.info("Created Bexio contact ${bexioContact.id}")
        return bexioContact
    }

    fun updateContact(
        bexioContactId: Int,
        request: BexioUpdateContactRequest,
    ) {
        bexioClient.updateContact(bexioContactId, request)
        logger.info("Updated Bexio contact $bexioContactId")
    }

    fun deleteContact(bexioContactId: Int) {
        bexioClient.deleteContact(bexioContactId)
        logger.info("Deleted Bexio contact $bexioContactId")
    }

    fun fetchContact(bexioContactId: Int): BexioContactResponse? = bexioClient.fetchContactById(bexioContactId)
}
