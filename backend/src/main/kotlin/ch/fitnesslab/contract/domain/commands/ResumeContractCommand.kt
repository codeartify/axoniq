package ch.fitnesslab.contract.domain.commands

import ch.fitnesslab.domain.value.ContractId
import org.axonframework.modelling.annotation.TargetEntityId

data class ResumeContractCommand(
    @TargetEntityId
    val contractId: ContractId,
)
