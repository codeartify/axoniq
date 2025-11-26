package ch.fitnesslab.billing.adapter.http

import ch.fitnesslab.billing.application.InvoiceProjection
import ch.fitnesslab.billing.application.InvoiceView
import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.billing.domain.commands.MarkInvoicePaidCommand
import ch.fitnesslab.common.types.InvoiceId
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@RestController
@CrossOrigin
@RequestMapping("/api/invoices")
class InvoiceController(
    private val invoiceProjection: InvoiceProjection,
    private val commandGateway: CommandGateway
) {

    @GetMapping
    fun getInvoices(@RequestParam(required = false) status: InvoiceStatus?): ResponseEntity<List<InvoiceDto>> {
        val invoices = if (status != null) {
            invoiceProjection.findByStatus(status)
        } else {
            invoiceProjection.findAll()
        }

        return ResponseEntity.ok(invoices.map { it.toDto() })
    }

    @GetMapping("/customer/{customerId}")
    fun getInvoicesByCustomerId(@PathVariable customerId: String): ResponseEntity<List<InvoiceDto>> {
        val invoices = invoiceProjection.findByCustomerId(customerId)
        return ResponseEntity.ok(invoices.map { it.toDto() })
    }

    @GetMapping("/{invoiceId}")
    fun getInvoiceById(@PathVariable invoiceId: String): ResponseEntity<InvoiceDto> {
        val invoice = invoiceProjection.findById(InvoiceId.from(invoiceId))
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(invoice.toDto())
    }

    @PostMapping("/{invoiceId}/pay")
    fun markAsPaid(@PathVariable invoiceId: String): ResponseEntity<Void> {
        commandGateway.sendAndWait<Any>(
            MarkInvoicePaidCommand(
                invoiceId = InvoiceId.from(invoiceId),
                paidAt = Instant.now()
            )
        )

        return ResponseEntity.ok().build()
    }
}

data class InvoiceDto(
    val invoiceId: String,
    val customerId: String,
    val bookingId: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val status: String,
    val isInstallment: Boolean,
    val installmentNumber: Int?,
    val paidAt: Instant?
)

private fun InvoiceView.toDto() = InvoiceDto(
    invoiceId = invoiceId,
    customerId = customerId,
    bookingId = bookingId,
    amount = amount,
    dueDate = dueDate,
    status = status.name,
    isInstallment = isInstallment,
    installmentNumber = installmentNumber,
    paidAt = paidAt
)
