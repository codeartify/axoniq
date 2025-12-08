package ch.fitnesslab.customers.domain.commands

import ch.fitnesslab.common.types.CustomerId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class LinkBexioContactCommand(
    @TargetAggregateIdentifier
    val customerId: CustomerId,
    val bexioContactId: Int
)
