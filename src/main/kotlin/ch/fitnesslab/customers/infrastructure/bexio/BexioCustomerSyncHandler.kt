package ch.fitnesslab.customers.infrastructure.bexio

import ch.fitnesslab.customers.domain.commands.LinkBexioContactCommand
import ch.fitnesslab.customers.domain.events.CustomerRegisteredEvent
import ch.fitnesslab.customers.domain.events.CustomerUpdatedEvent
import ch.fitnesslab.customers.infrastructure.CustomerRepository
import ch.fitnesslab.domain.value.BexioContactId
import ch.fitnesslab.domain.value.Country
import ch.fitnesslab.domain.value.Salutation
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("bexio-sync")
class BexioCustomerSyncHandler(
    private val bexioContactService: BexioContactService,
    private val customerRepository: CustomerRepository,
    private val commandGateway: CommandGateway,
) {
    private val logger = LoggerFactory.getLogger(BexioCustomerSyncHandler::class.java)

    @EventHandler
    fun on(event: CustomerRegisteredEvent) {
        try {
            // Create contact in Bexio directly from event data (no dependency on projection)
            val request =
                BexioCreateContactRequest(
                    name1 = "${event.firstName} ${event.lastName}",
                    name2 = null,
                    salutationId = mapSalutationToBexioId(event.salutation),
                    streetName = event.address.street.value,
                    houseNumber = event.address.houseNumber.value,
                    postcode = event.address.postalCode.value,
                    city = event.address.city.value,
                    countryId = mapCountryToBexio(event.address.country),
                    mail = event.email.value,
                    phoneMobile = event.phoneNumber?.value,
                    languageId = 1, // Default to German TODO: make configurable
                    contactTypeId = 2, // Required: 1 = Company, 2 = Person - TODO: make configurable
                    userId = 1, // Required: User ID in Bexio - TODO: make configurable
                    ownerId = 1, // Required: Owner ID in Bexio - TODO: make configurable
                )

            val bexioContact = bexioContactService.createContact(request)

            // Link the Bexio contact ID to the customer aggregate
            commandGateway.sendAndWait<Any>(
                LinkBexioContactCommand(
                    customerId = event.customerId,
                    bexioContactId = BexioContactId.of(bexioContact.id),
                ),
            )

            logger.info("Successfully synced customer ${event.customerId} to Bexio contact ${bexioContact.id}")
        } catch (e: Exception) {
            logger.error("Failed to sync customer ${event.customerId} to Bexio: ${e.message}", e)
            // Note: We don't throw here to avoid blocking the event stream
        }
    }

    @EventHandler
    fun on(event: CustomerUpdatedEvent) {
        try {
            // Get customer entity to retrieve bexioContactId
            val customer = customerRepository.findById(event.customerId.value).orElse(null)
            if (customer == null) {
                logger.warn("Customer ${event.customerId} not found in projection, cannot sync to Bexio")
                return
            }

            if (customer.bexioContactId == null) {
                logger.warn("Customer ${event.customerId} does not have a Bexio contact ID, skipping update")
                return
            }

            // Update contact in Bexio using event data
            val request =
                BexioUpdateContactRequest(
                    name1 = "${event.firstName} ${event.lastName}",
                    name2 = null,
                    salutationId = mapSalutationToBexioId(event.salutation),
                    streetName = event.address.street.value,
                    houseNumber = event.address.houseNumber.value,
                    postcode = event.address.postalCode.value,
                    city = event.address.city.value,
                    countryId = mapCountryToBexio(event.address.country),
                    mail = event.email.value,
                    phoneMobile = event.phoneNumber?.value,
                )

            bexioContactService.updateContact(customer.bexioContactId!!, request)

            logger.info("Successfully synced customer ${event.customerId} update to Bexio contact ${customer.bexioContactId}")
        } catch (e: Exception) {
            logger.error("Failed to sync customer ${event.customerId} update to Bexio: ${e.message}", e)
        }
    }

    private fun mapSalutationToBexioId(salutation: Salutation): Int =
        when (salutation) {
            Salutation.MR -> 1
            Salutation.MS -> 2
            else -> 3
        }

    private fun mapCountryToBexio(country: Country): Int =
        when (country.value.uppercase()) {
            "SWITZERLAND", "SCHWEIZ", "SUISSE", "CH" -> 1
            "GERMANY", "DEUTSCHLAND", "DE" -> 2
            "AUSTRIA", "ÖSTERREICH", "AT" -> 3
            "FRANCE", "FRANKREICH", "FR" -> 4
            "ITALY", "ITALIEN", "IT" -> 5
            else -> 1
        }
}
