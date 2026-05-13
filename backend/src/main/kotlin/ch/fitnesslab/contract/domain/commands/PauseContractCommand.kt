package ch.fitnesslab.contract.domain.commands

import ch.fitnesslab.domain.PauseReason
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.DateRange
import org.axonframework.modelling.annotation.TargetEntityId

data class PauseContractCommand(
    @TargetEntityId
    val contractId: ContractId,
    val pauseRange: DateRange,
    val reason: PauseReason,
)
