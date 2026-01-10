package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CancelInvoiceCommand(
    @TargetAggregateIdentifier
    val invoiceId: InvoiceId,
    val reason: String,
)
