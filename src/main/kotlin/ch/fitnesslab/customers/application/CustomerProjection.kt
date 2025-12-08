package ch.fitnesslab.customers.application

import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.customers.domain.events.BexioContactLinkedEvent
import ch.fitnesslab.customers.domain.events.CustomerRegisteredEvent
import ch.fitnesslab.customers.domain.events.CustomerUpdatedEvent
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.customers.infrastructure.CustomerRepository
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
                phoneNumber = event.phoneNumber,
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
        customerRepository.findById(event.customerId.value).ifPresent { existing ->
            val updated =
                CustomerEntity(
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
                    phoneNumber = event.phoneNumber,
                    bexioContactId = existing.bexioContactId,
                )
            customerRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllCustomersQuery::class.java,
                { true },
                CustomerUpdatedUpdate(event.customerId.value.toString()),
            )
        }
    }

    @EventHandler
    fun on(event: BexioContactLinkedEvent) {
        customerRepository.findById(event.customerId.value).ifPresent { existing ->
            val updated =
                CustomerEntity(
                    customerId = existing.customerId,
                    salutation = existing.salutation,
                    firstName = existing.firstName,
                    lastName = existing.lastName,
                    dateOfBirth = existing.dateOfBirth,
                    street = existing.street,
                    houseNumber = existing.houseNumber,
                    postalCode = existing.postalCode,
                    city = existing.city,
                    country = existing.country,
                    email = existing.email,
                    phoneNumber = existing.phoneNumber,
                    bexioContactId = event.bexioContactId,
                )
            customerRepository.save(updated)

            queryUpdateEmitter.emit(
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
            .findById(customerId.value)
            .orElse(null)

    fun findAll(): List<CustomerEntity> = customerRepository.findAll()
}
