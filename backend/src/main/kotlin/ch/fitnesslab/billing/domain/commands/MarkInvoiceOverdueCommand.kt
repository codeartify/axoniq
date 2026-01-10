package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class MarkInvoiceOverdueCommand(
    @TargetAggregateIdentifier
    val invoiceId: InvoiceId,
)
