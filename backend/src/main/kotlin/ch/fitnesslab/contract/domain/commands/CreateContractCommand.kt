package ch.fitnesslab.contract.domain.commands

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateContractCommand(
    @TargetAggregateIdentifier
    val contractId: ContractId,
    val customerId: CustomerId,
    val productId: ProductId,
    val bookingId: BookingId,
    val validity: DateRange?,
    val sessionsTotal: Int?,
)
