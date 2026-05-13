package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.eventsourcing.annotation.EventTag

data class InvoiceMarkedOverdueEvent(
    @field:EventTag(key = "Invoice")
    val invoiceId: InvoiceId,
)
