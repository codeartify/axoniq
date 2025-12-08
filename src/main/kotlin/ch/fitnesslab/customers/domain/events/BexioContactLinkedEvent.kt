package ch.fitnesslab.customers.domain.events

import ch.fitnesslab.common.types.CustomerId

data class BexioContactLinkedEvent(
    val customerId: CustomerId,
    val bexioContactId: Int
)
