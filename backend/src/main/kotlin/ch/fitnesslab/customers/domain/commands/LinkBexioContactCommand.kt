package ch.fitnesslab.customers.domain.commands

import ch.fitnesslab.domain.value.BexioContactId
import ch.fitnesslab.domain.value.CustomerId
import org.axonframework.modelling.annotation.TargetEntityId

data class LinkBexioContactCommand(
    @TargetEntityId
    val customerId: CustomerId,
    val bexioContactId: BexioContactId,
)
