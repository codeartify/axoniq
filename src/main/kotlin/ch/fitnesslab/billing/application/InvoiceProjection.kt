package ch.fitnesslab.billing.application

import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.billing.domain.events.InvoiceCreatedEvent
import ch.fitnesslab.billing.domain.events.InvoicePaidEvent
import ch.fitnesslab.common.types.InvoiceId
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

@Component
class InvoiceProjection {

    private val invoices = ConcurrentHashMap<InvoiceId, InvoiceView>()

    @EventHandler
    fun on(event: InvoiceCreatedEvent) {
        invoices[event.invoiceId] = InvoiceView(
            invoiceId = event.invoiceId.toString(),
            customerId = event.customerId.toString(),
            bookingId = event.bookingId.toString(),
            amount = event.amount,
            dueDate = event.dueDate,
            status = event.status,
            isInstallment = event.isInstallment,
            installmentNumber = event.installmentNumber,
            paidAt = null
        )
    }

    @EventHandler
    fun on(event: InvoicePaidEvent) {
        invoices.computeIfPresent(event.invoiceId) { _, invoice ->
            invoice.copy(
                status = InvoiceStatus.PAID,
                paidAt = event.paidAt
            )
        }
    }

    fun findAll(): List<InvoiceView> = invoices.values.toList()

    fun findByStatus(status: InvoiceStatus): List<InvoiceView> =
        invoices.values.filter { it.status == status }

    fun findById(invoiceId: InvoiceId): InvoiceView? = invoices[invoiceId]
}

data class InvoiceView(
    val invoiceId: String,
    val customerId: String,
    val bookingId: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val status: InvoiceStatus,
    val isInstallment: Boolean,
    val installmentNumber: Int?,
    val paidAt: Instant?
)
