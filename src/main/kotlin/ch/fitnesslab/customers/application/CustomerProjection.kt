package ch.fitnesslab.customers.application

import ch.fitnesslab.common.types.Address
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.Salutation
import ch.fitnesslab.customers.domain.events.CustomerRegisteredEvent
import ch.fitnesslab.customers.domain.events.CustomerUpdatedEvent
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Component
class CustomerProjection {

    private val customers = ConcurrentHashMap<CustomerId, CustomerView>()

    @EventHandler
    fun on(event: CustomerRegisteredEvent) {
        customers[event.customerId] = CustomerView(
            customerId = event.customerId.toString(),
            salutation = event.salutation,
            firstName = event.firstName,
            lastName = event.lastName,
            dateOfBirth = event.dateOfBirth,
            address = event.address,
            email = event.email,
            phoneNumber = event.phoneNumber
        )
    }

    @EventHandler
    fun on(event: CustomerUpdatedEvent) {
        customers.computeIfPresent(event.customerId) { _, customer ->
            customer.copy(
                salutation = event.salutation,
                firstName = event.firstName,
                lastName = event.lastName,
                dateOfBirth = event.dateOfBirth,
                address = event.address,
                email = event.email,
                phoneNumber = event.phoneNumber
            )
        }
    }

    fun findById(customerId: CustomerId): CustomerView? = customers[customerId]

    fun findAll(): List<CustomerView> = customers.values.toList()
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
