package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.eventsourcing.annotation.EventTag
import java.time.Instant

data class InvoicePaidEvent(
    @field:EventTag(key = "Invoice")
    val invoiceId: InvoiceId,
    val paidAt: Instant,
)
