package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.modelling.annotation.TargetEntityId
import java.time.Instant

data class MarkInvoicePaidCommand(
    @TargetEntityId
    val invoiceId: InvoiceId,
    val paidAt: Instant = Instant.now(),
)
