package ch.fitnesslab.customers.domain.commands

import ch.fitnesslab.domain.value.BexioContactId
import ch.fitnesslab.domain.value.CustomerId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class LinkBexioContactCommand(
    @TargetAggregateIdentifier
    val customerId: CustomerId,
    val bexioContactId: BexioContactId,
)
