package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductContractId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class PauseProductContractCommand(
    @TargetAggregateIdentifier
    val contractId: ProductContractId,
    val pauseRange: DateRange,
    val reason: PauseReason,
)
