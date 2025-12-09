package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.serialization.Revision
import java.time.Instant

@Revision("1.0")
data class InvoicePaidEvent(
    val invoiceId: InvoiceId,
    val paidAt: Instant,
)
