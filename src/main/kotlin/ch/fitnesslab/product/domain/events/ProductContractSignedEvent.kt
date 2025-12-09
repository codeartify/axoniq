package ch.fitnesslab.product.domain.events

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductContractId
import ch.fitnesslab.domain.value.ProductVariantId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ProductContractSignedEvent(
    val contractId: ProductContractId,
    val customerId: CustomerId,
    val productVariantId: ProductVariantId,
    val bookingId: BookingId,
    val validity: DateRange?,
    val sessionsTotal: Int?,
)
