package ch.fitnesslab.customers.domain.events

import ch.fitnesslab.domain.value.BexioContactId
import ch.fitnesslab.domain.value.CustomerId
import org.axonframework.eventsourcing.annotation.EventTag

data class BexioContactLinkedEvent(
    @field:EventTag(key = "Customer")
    val customerId: CustomerId,
    val bexioContactId: BexioContactId,
)
