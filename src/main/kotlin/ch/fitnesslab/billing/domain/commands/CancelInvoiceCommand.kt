package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.common.types.InvoiceId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CancelInvoiceCommand(
    @TargetAggregateIdentifier
    val invoiceId: InvoiceId,
    val reason: String,
)
