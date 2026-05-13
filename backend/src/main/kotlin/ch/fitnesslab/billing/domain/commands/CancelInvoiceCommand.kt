package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.modelling.annotation.TargetEntityId

data class CancelInvoiceCommand(
    @TargetEntityId
    val invoiceId: InvoiceId,
    val reason: String,
)
