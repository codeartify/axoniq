package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.common.types.InvoiceId
import java.time.Instant

data class InvoicePaidEvent(
    val invoiceId: InvoiceId,
    val paidAt: Instant
)
