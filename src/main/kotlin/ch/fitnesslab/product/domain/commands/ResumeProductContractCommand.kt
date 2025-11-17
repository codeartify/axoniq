package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.common.types.ProductContractId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class ResumeProductContractCommand(
    @TargetAggregateIdentifier
    val contractId: ProductContractId
)
