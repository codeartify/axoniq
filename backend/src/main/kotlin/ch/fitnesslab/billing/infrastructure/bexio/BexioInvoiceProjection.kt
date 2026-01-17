package ch.fitnesslab.billing.infrastructure.bexio

import ch.fitnesslab.billing.domain.events.InvoiceCreatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler

@ProcessingGroup("bexio-invoice")
class BexioInvoiceProjection(
    private val bexioInvoiceService: BexioInvoiceService,
) {
    @EventHandler
    fun on(event: InvoiceCreatedEvent) {
        bexioInvoiceService.createInvoiceInBexio(
            invoiceId = event.invoiceId,
            customerId = event.customerId,
            productId = event.productId,
            amount = event.amount,
            dueDate = event.dueDate.value,
        )
    }
}
