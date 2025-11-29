package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.common.types.ProductVariantId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateProductContractCommand(
    @TargetAggregateIdentifier
    val contractId: ProductContractId,
    val customerId: CustomerId,
    val productVariantId: ProductVariantId,
    val bookingId: BookingId,
    val validity: DateRange?,
    val sessionsTotal: Int?,
)
