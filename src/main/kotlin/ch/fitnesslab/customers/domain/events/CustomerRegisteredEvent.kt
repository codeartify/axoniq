package ch.fitnesslab.customers.domain.events

import ch.fitnesslab.customers.domain.value.DateOfBirth
import ch.fitnesslab.customers.domain.value.EmailAddress
import ch.fitnesslab.customers.domain.value.FirstName
import ch.fitnesslab.customers.domain.value.LastName
import ch.fitnesslab.customers.domain.value.PhoneNumber
import ch.fitnesslab.domain.value.Address
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.Salutation
import org.axonframework.serialization.Revision

@Revision("1.0")
data class CustomerRegisteredEvent(
    val customerId: CustomerId,
    val salutation: Salutation,
    val firstName: FirstName,
    val lastName: LastName,
    val dateOfBirth: DateOfBirth,
    val address: Address,
    val email: EmailAddress,
    val phoneNumber: PhoneNumber?,
)
