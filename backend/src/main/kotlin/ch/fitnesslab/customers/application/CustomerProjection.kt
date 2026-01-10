package ch.fitnesslab.customers.application

import ch.fitnesslab.customers.domain.events.BexioContactLinkedEvent
import ch.fitnesslab.customers.domain.events.CustomerRegisteredEvent
import ch.fitnesslab.customers.domain.events.CustomerUpdatedEvent
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.customers.infrastructure.CustomerRepository
import ch.fitnesslab.domain.value.CustomerId
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("customers")
class CustomerProjection(
    private val customerRepository: CustomerRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter,
) {
    @EventHandler
    fun on(event: CustomerRegisteredEvent) {
        val entity =
            CustomerEntity(
                customerId = event.customerId.value.toString(),
                salutation = event.salutation.name,
                firstName = event.firstName.value,
                lastName = event.lastName.value,
                dateOfBirth = event.dateOfBirth.value,
                street = event.address.street.value,
                houseNumber = event.address.houseNumber.value,
                postalCode = event.address.postalCode.value,
                city = event.address.city.value,
                country = event.address.country.value,
                email = event.email.value,
                phoneNumber = event.phoneNumber?.value,
            )
        customerRepository.save(entity)

        queryUpdateEmitter.emit(
            FindAllCustomersQuery::class.java,
            { true },
            CustomerUpdatedUpdate(event.customerId.value.toString()),
        )
    }

    @EventHandler
    fun on(event: CustomerUpdatedEvent) {
        customerRepository.findById(event.customerId.value.toString()).ifPresent { existing ->
            existing.customerId = event.customerId.value.toString()
            existing.salutation = event.salutation.name
            existing.firstName = event.firstName.value
            existing.lastName = event.lastName.value
            existing.dateOfBirth = event.dateOfBirth.value
            existing.street = event.address.street.value
            existing.houseNumber = event.address.houseNumber.value
            existing.postalCode = event.address.postalCode.value
            existing.city = event.address.city.value
            existing.country = event.address.country.value
            existing.email = event.email.value
            existing.phoneNumber = event.phoneNumber?.value

            customerRepository.save(existing)

            queryUpdateEmitter.emit(
                FindAllCustomersQuery::class.java, // TODO: emit also FindCustomerByIdQuery?
                { true },
                CustomerUpdatedUpdate(event.customerId.value.toString()),
            )
        }
    }

    @EventHandler
    fun on(event: BexioContactLinkedEvent) {
        customerRepository.findById(event.customerId.value.toString()).ifPresent { existing ->
            existing.bexioContactId = event.bexioContactId.value
            customerRepository.save(existing)

            queryUpdateEmitter.emit(
                // TODO: emit also FindCustomerByIdQuery?
                FindAllCustomersQuery::class.java,
                { true },
                CustomerUpdatedUpdate(event.customerId.value.toString()),
            )
        }
    }

    @QueryHandler
    fun handle(query: FindAllCustomersQuery): List<CustomerEntity> = findAll()

    @QueryHandler
    fun handle(query: FindCustomerByIdQuery): CustomerEntity? = findById(query.customerId)

    fun findById(customerId: CustomerId): CustomerEntity? =
        customerRepository
            .findById(customerId.value.toString())
            .orElse(null)

    fun findAll(): List<CustomerEntity> = customerRepository.findAll()
}
