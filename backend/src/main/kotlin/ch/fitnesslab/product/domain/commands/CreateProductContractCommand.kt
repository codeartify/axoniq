package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductContractId
import ch.fitnesslab.domain.value.ProductVariantId
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
