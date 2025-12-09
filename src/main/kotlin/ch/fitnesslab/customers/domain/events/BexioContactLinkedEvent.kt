package ch.fitnesslab.customers.domain.events

import ch.fitnesslab.domain.value.BexioContactId
import ch.fitnesslab.domain.value.CustomerId

data class BexioContactLinkedEvent(
    val customerId: CustomerId,
    val bexioContactId: BexioContactId,
)
