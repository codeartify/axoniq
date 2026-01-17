package ch.fitnesslab.contract.domain.commands

import ch.fitnesslab.domain.value.ContractId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class ResumeContractCommand(
    @TargetAggregateIdentifier
    val contractId: ContractId,
)
