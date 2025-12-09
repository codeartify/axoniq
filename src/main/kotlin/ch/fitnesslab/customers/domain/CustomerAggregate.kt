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
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class CustomerAggregate() {
    @AggregateIdentifier
    private lateinit var customerId: CustomerId
    private lateinit var salutation: Salutation
    private lateinit var firstName: FirstName
    private lateinit var lastName: LastName
    private lateinit var dateOfBirth: DateOfBirth
    private lateinit var address: Address
    private lateinit var email: EmailAddress
    private var phoneNumber: PhoneNumber? = null
    private var bexioContactId: BexioContactId? = null // could be more generic to accomodate other IDs, e.g. as a Map<System, ID>

    @CommandHandler
    constructor(command: RegisterCustomerCommand) : this() {

        AggregateLifecycle.apply(
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

    @CommandHandler
    fun handle(command: UpdateCustomerCommand) {
        AggregateLifecycle.apply(
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
    fun handle(command: LinkBexioContactCommand) {
        AggregateLifecycle.apply(
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
