package ch.fitnesslab.customers.domain

import ch.fitnesslab.customers.domain.commands.LinkBexioContactCommand
import ch.fitnesslab.customers.domain.commands.RegisterCustomerCommand
import ch.fitnesslab.customers.domain.commands.UpdateCustomerCommand
import ch.fitnesslab.customers.domain.events.BexioContactLinkedEvent
import ch.fitnesslab.customers.domain.events.CustomerRegisteredEvent
import ch.fitnesslab.customers.domain.events.CustomerUpdatedEvent
import ch.fitnesslab.customers.domain.value.*
import ch.fitnesslab.domain.value.Address
import ch.fitnesslab.domain.value.BexioContactId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.Salutation
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.messaging.eventhandling.gateway.EventAppender

@EventSourced(idType = CustomerId::class, tagKey = "Customer")
class CustomerAggregate {
    private lateinit var customerId: CustomerId
    private lateinit var salutation: Salutation
    private lateinit var firstName: FirstName
    private lateinit var lastName: LastName
    private lateinit var dateOfBirth: DateOfBirth
    private lateinit var address: Address
    private lateinit var email: EmailAddress
    private var phoneNumber: PhoneNumber? = null
    private var bexioContactId: BexioContactId? = null

    @EntityCreator
    constructor()

    companion object {
        @JvmStatic
        @CommandHandler
        fun handle(
            command: RegisterCustomerCommand,
            eventAppender: EventAppender,
        ) {
            eventAppender.append(
                CustomerRegisteredEvent(
                    customerId = command.customerId,
                    salutation = command.salutation,
                    firstName = command.firstName,
                    lastName = command.lastName,
                    dateOfBirth = command.dateOfBirth,
                    address = command.address,
                    email = command.email,
                    phoneNumber = command.phoneNumber,
                ),
            )
        }
    }

    @CommandHandler
    fun handle(
        command: UpdateCustomerCommand,
        eventAppender: EventAppender,
    ) {
        eventAppender.append(
            CustomerUpdatedEvent(
                customerId = command.customerId,
                salutation = command.salutation,
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = command.dateOfBirth,
                address = command.address,
                email = command.email,
                phoneNumber = command.phoneNumber,
            ),
        )
    }

    @CommandHandler
    fun handle(
        command: LinkBexioContactCommand,
        eventAppender: EventAppender,
    ) {
        eventAppender.append(
            BexioContactLinkedEvent(
                customerId = command.customerId,
                bexioContactId = command.bexioContactId,
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: CustomerRegisteredEvent) {
        this.customerId = event.customerId
        this.salutation = event.salutation
        this.firstName = event.firstName
        this.lastName = event.lastName
        this.dateOfBirth = event.dateOfBirth
        this.address = event.address
        this.email = event.email
        this.phoneNumber = event.phoneNumber
    }

    @EventSourcingHandler
    fun on(event: CustomerUpdatedEvent) {
        this.salutation = event.salutation
        this.firstName = event.firstName
        this.lastName = event.lastName
        this.dateOfBirth = event.dateOfBirth
        this.address = event.address
        this.email = event.email
        this.phoneNumber = event.phoneNumber
    }

    @EventSourcingHandler
    fun on(event: BexioContactLinkedEvent) {
        this.bexioContactId = event.bexioContactId
    }
}
