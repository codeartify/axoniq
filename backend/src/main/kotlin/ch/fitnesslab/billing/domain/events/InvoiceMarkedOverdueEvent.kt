package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class InvoiceMarkedOverdueEvent(
    val invoiceId: InvoiceId,
)
