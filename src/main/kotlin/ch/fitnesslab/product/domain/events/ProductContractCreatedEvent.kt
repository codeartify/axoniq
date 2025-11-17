package ch.fitnesslab.product.domain.events

import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.ProductContractStatus

data class ProductContractCreatedEvent(
    val contractId: ProductContractId,
    val customerId: CustomerId,
    val productVariantId: ProductVariantId,
    val bookingId: BookingId,
    val status: ProductContractStatus,
    val validity: DateRange?,
    val sessionsTotal: Int?
)
