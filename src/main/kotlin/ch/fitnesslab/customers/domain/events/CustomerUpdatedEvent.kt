package ch.fitnesslab.customers.domain.events

import ch.fitnesslab.common.types.Address
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.Salutation
import org.axonframework.serialization.Revision
import java.time.LocalDate

@Revision("1.0")
data class CustomerUpdatedEvent(
    val customerId: CustomerId,
    val salutation: Salutation,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val address: Address,
    val email: String,
    val phoneNumber: String?,
)
