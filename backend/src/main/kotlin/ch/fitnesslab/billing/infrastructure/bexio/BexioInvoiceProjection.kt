package ch.fitnesslab.billing.infrastructure.bexio

import ch.fitnesslab.billing.domain.events.InvoiceCreatedEvent
import ch.fitnesslab.plugins.bexio.invoice.BexioInvoiceAdapter
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler

@ProcessingGroup("bexio-invoice")
class BexioInvoiceProjection(
    private val bexioInvoiceAdapter: BexioInvoiceAdapter,
) {
    @EventHandler
    fun on(event: InvoiceCreatedEvent) {
        bexioInvoiceAdapter.createInvoiceInBexio(
            invoiceId = event.invoiceId,
            customerId = event.customerId,
            productId = event.productId,
            amount = event.amount,
            dueDate = event.dueDate.value,
        )
    }
}
