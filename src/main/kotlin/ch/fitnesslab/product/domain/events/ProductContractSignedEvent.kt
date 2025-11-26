package ch.fitnesslab.product.domain.events

import ch.fitnesslab.common.types.*
import ch.fitnesslab.product.domain.ProductContractStatus
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ProductContractSignedEvent(
    val contractId: ProductContractId,
    val customerId: CustomerId,
    val productVariantId: ProductVariantId,
    val bookingId: BookingId,
    val status: ProductContractStatus,
    val validity: DateRange?,
    val sessionsTotal: Int?
)
