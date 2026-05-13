package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.eventsourcing.annotation.EventTag

data class InvoiceCancelledEvent(
    @field:EventTag(key = "Invoice")
    val invoiceId: InvoiceId,
    val reason: String,
)
