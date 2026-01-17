package ch.fitnesslab.contract.domain.events

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ContractSignedEvent(
    val contractId: ContractId,
    val customerId: CustomerId,
    val productId: ProductId,
    val bookingId: BookingId,
    val validity: DateRange?,
    val sessionsTotal: Int?,
)
