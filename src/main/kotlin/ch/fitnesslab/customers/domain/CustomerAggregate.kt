package ch.fitnesslab.customers.domain

import ch.fitnesslab.common.types.Address
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.Salutation
import ch.fitnesslab.customers.domain.commands.RegisterCustomerCommand
import ch.fitnesslab.customers.domain.commands.UpdateCustomerCommand
import ch.fitnesslab.customers.domain.events.CustomerRegisteredEvent
import ch.fitnesslab.customers.domain.events.CustomerUpdatedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.time.LocalDate

@Aggregate
class CustomerAggregate() {
    @AggregateIdentifier
    private lateinit var customerId: CustomerId
    private lateinit var salutation: Salutation
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var dateOfBirth: LocalDate
    private lateinit var address: Address
    private lateinit var email: String
    private var phoneNumber: String? = null

    @CommandHandler
    constructor(command: RegisterCustomerCommand) : this() {
        // Validation
        require(command.firstName.isNotBlank()) { "First name cannot be blank" }
        require(command.lastName.isNotBlank()) { "Last name cannot be blank" }
        require(command.email.isNotBlank()) { "Email cannot be blank" }
        require(command.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "Invalid email format"
        }
        require(command.dateOfBirth.isBefore(LocalDate.now())) {
            "Date of birth must be in the past"
        }
        val age = LocalDate.now().year - command.dateOfBirth.year
        require(age >= 16) { "Customer must be at least 16 years old" }

        if (command.phoneNumber != null) {
            require(command.phoneNumber!!.isNotBlank()) { "Phone number cannot be blank if provided" }
        }

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
        // Validation
        require(command.firstName.isNotBlank()) { "First name cannot be blank" }
        require(command.lastName.isNotBlank()) { "Last name cannot be blank" }
        require(command.email.isNotBlank()) { "Email cannot be blank" }
        require(command.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "Invalid email format"
        }
        require(command.dateOfBirth.isBefore(LocalDate.now())) {
            "Date of birth must be in the past"
        }
        val age = LocalDate.now().year - command.dateOfBirth.year
        require(age >= 16) { "Customer must be at least 16 years old" }

        if (command.phoneNumber != null) {
            require(command.phoneNumber!!.isNotBlank()) { "Phone number cannot be blank if provided" }
        }

        AggregateLifecycle.apply(
            CustomerUpdatedEvent(
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
}
