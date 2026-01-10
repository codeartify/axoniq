package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.time.Instant

data class MarkInvoicePaidCommand(
    @TargetAggregateIdentifier
    val invoiceId: InvoiceId,
    val paidAt: Instant = Instant.now(),
)
