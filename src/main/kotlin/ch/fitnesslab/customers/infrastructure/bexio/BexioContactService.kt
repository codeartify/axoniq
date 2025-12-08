package ch.fitnesslab.customers.infrastructure.bexio

import ch.fitnesslab.billing.infrastructure.bexio.BexioClient
import ch.fitnesslab.common.types.Salutation
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BexioContactService(
    private val bexioClient: BexioClient
) {
    private val logger = LoggerFactory.getLogger(BexioContactService::class.java)

    fun createContactFromCustomer(customer: CustomerEntity): Int {
        val request = BexioCreateContactRequest(
            name1 = "${customer.firstName} ${customer.lastName}",
            name2 = null,
            salutationId = mapSalutationToBexioId(customer.salutation),
            streetName = customer.street,
            houseNumber = customer.houseNumber,
            postcode = customer.postalCode,
            city = customer.city,
            countryId = mapCountryToBexioId(customer.country),
            mail = customer.email,
            phoneMobile = customer.phoneNumber,
            languageId = 1, // Default to German, TODO: make configurable
            contactTypeId = 2, // 1=Company, 2=Person - TODO: make configurable
            userId = 1, // TODO: make configurable
            ownerId = 1 // TODO: make configurable
        )

        val bexioContact = bexioClient.createContact(request)
        logger.info("Created Bexio contact ${bexioContact.id} for customer ${customer.customerId}")

        return bexioContact.id
    }

    fun updateContactFromCustomer(bexioContactId: Int, customer: CustomerEntity) {
        val request = BexioUpdateContactRequest(
            name1 = "${customer.firstName} ${customer.lastName}",
            name2 = null,
            salutationId = mapSalutationToBexioId(customer.salutation),
            streetName = customer.street,
            houseNumber = customer.houseNumber,
            postcode = customer.postalCode,
            city = customer.city,
            countryId = mapCountryToBexioId(customer.country),
            mail = customer.email,
            phoneMobile = customer.phoneNumber
        )

        bexioClient.updateContact(bexioContactId, request)
        logger.info("Updated Bexio contact $bexioContactId for customer ${customer.customerId}")
    }

    fun deleteContact(bexioContactId: Int) {
        bexioClient.deleteContact(bexioContactId)
        logger.info("Deleted Bexio contact $bexioContactId")
    }

    fun createContact(request: BexioCreateContactRequest): BexioContactDto {
        val bexioContact = bexioClient.createContact(request)
        logger.info("Created Bexio contact ${bexioContact.id}")
        return bexioContact
    }

    fun updateContact(bexioContactId: Int, request: BexioUpdateContactRequest) {
        bexioClient.updateContact(bexioContactId, request)
        logger.info("Updated Bexio contact $bexioContactId")
    }

    private fun mapSalutationToBexioId(salutation: Salutation): Int {
        return when (salutation) {
            Salutation.MR -> 1 // Herr
            Salutation.MS -> 2 // Frau
            else -> 3 // Divers or neutral
        }
    }

    private fun mapCountryToBexioId(country: String): Int {
        // Bexio countrynp IDs - these are approximate, you should verify with Bexio API
        return when (country.uppercase()) {
            "SWITZERLAND", "SCHWEIZ", "SUISSE", "CH" -> 1
            "GERMANY", "DEUTSCHLAND", "DE" -> 2
            "AUSTRIA", "ÖSTERREICH", "AT" -> 3
            "FRANCE", "FRANKREICH", "FR" -> 4
            "ITALY", "ITALIEN", "IT" -> 5
            else -> 1 // Default to Switzerland
        }
    }
}
