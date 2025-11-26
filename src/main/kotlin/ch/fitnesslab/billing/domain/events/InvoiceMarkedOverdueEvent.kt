package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.common.types.InvoiceId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class InvoiceMarkedOverdueEvent(
    val invoiceId: InvoiceId
)
