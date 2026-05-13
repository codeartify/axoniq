package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.modelling.annotation.TargetEntityId

data class MarkInvoiceOverdueCommand(
    @TargetEntityId
    val invoiceId: InvoiceId,
)
