package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.domain.value.ProductContractId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class ResumeProductContractCommand(
    @TargetAggregateIdentifier
    val contractId: ProductContractId,
)
