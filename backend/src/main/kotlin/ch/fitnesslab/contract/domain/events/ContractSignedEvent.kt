package ch.fitnesslab.contract.domain.events

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.ProductVariantId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ContractSignedEvent(
    val contractId: ContractId,
    val customerId: CustomerId,
    val productVariantId: ProductVariantId,
    val bookingId: BookingId,
    val validity: DateRange?,
    val sessionsTotal: Int?,
)
