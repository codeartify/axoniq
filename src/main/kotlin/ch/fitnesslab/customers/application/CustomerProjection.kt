package ch.fitnesslab.customers.application

import ch.fitnesslab.common.types.Address
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.Salutation
import ch.fitnesslab.customers.domain.events.CustomerRegisteredEvent
import ch.fitnesslab.customers.domain.events.CustomerUpdatedEvent
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.customers.infrastructure.CustomerRepository
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@ProcessingGroup("customers")
class CustomerProjection(
    private val customerRepository: CustomerRepository
) {

    @EventHandler
    fun on(event: CustomerRegisteredEvent) {
        val entity = CustomerEntity(
            customerId = event.customerId.value,
            salutation = event.salutation,
            firstName = event.firstName,
            lastName = event.lastName,
            dateOfBirth = event.dateOfBirth,
            street = event.address.street,
            houseNumber = event.address.houseNumber,
            postalCode = event.address.postalCode,
            city = event.address.city,
            country = event.address.country,
            email = event.email,
            phoneNumber = event.phoneNumber
        )
        customerRepository.save(entity)
    }

    @EventHandler
    fun on(event: CustomerUpdatedEvent) {
        customerRepository.findById(event.customerId.value).ifPresent { existing ->
            val updated = CustomerEntity(
                customerId = existing.customerId,
                salutation = event.salutation,
                firstName = event.firstName,
                lastName = event.lastName,
                dateOfBirth = event.dateOfBirth,
                street = event.address.street,
                houseNumber = event.address.houseNumber,
                postalCode = event.address.postalCode,
                city = event.address.city,
                country = event.address.country,
                email = event.email,
                phoneNumber = event.phoneNumber
            )
            customerRepository.save(updated)
        }
    }

    fun findById(customerId: CustomerId): CustomerView? {
        return customerRepository.findById(customerId.value)
            .map { it.toCustomerView() }
            .orElse(null)
    }

    fun findAll(): List<CustomerView> {
        return customerRepository.findAll().map { it.toCustomerView() }
    }

    private fun CustomerEntity.toCustomerView() = CustomerView(
        customerId = this.customerId.toString(),
        salutation = this.salutation,
        firstName = this.firstName,
        lastName = this.lastName,
        dateOfBirth = this.dateOfBirth,
        address = Address(
            street = this.street,
            houseNumber = this.houseNumber,
            postalCode = this.postalCode,
            city = this.city,
            country = this.country
        ),
        email = this.email,
        phoneNumber = this.phoneNumber
    )
}

data class CustomerView(
    val customerId: String,
    val salutation: Salutation,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val address: Address,
    val email: String,
    val phoneNumber: String?
)
