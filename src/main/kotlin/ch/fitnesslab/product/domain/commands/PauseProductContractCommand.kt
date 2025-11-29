package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class PauseProductContractCommand(
    @TargetAggregateIdentifier
    val contractId: ProductContractId,
    val pauseRange: DateRange,
    val reason: PauseReason,
)
