package ch.fitnesslab.billing.adapter.http

import ch.fitnesslab.billing.application.FindAllInvoicesQuery
import ch.fitnesslab.billing.application.InvoiceProjection
import ch.fitnesslab.billing.application.InvoiceUpdatedUpdate
import ch.fitnesslab.billing.application.InvoiceView
import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.billing.domain.commands.CancelInvoiceCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoiceOverdueCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoicePaidCommand
import ch.fitnesslab.common.types.InvoiceId
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@RestController
@CrossOrigin
@RequestMapping("/api/invoices")
class InvoiceController(
    private val invoiceProjection: InvoiceProjection,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
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
        val subscriptionQuery = queryGateway.subscriptionQuery(
            FindAllInvoicesQuery(),
            ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
            ResponseTypes.instanceOf(InvoiceUpdatedUpdate::class.java)
        )

        try {
            commandGateway.sendAndWait<Any>(
                MarkInvoicePaidCommand(
                    invoiceId = InvoiceId.from(invoiceId),
                    paidAt = Instant.now()
                )
            )

            // Wait for projection update
            subscriptionQuery.updates().blockFirst(Duration.ofSeconds(5))

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    @PostMapping("/{invoiceId}/mark-overdue")
    fun markAsOverdue(@PathVariable invoiceId: String): ResponseEntity<Void> {
        val subscriptionQuery = queryGateway.subscriptionQuery(
            FindAllInvoicesQuery(),
            ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
            ResponseTypes.instanceOf(InvoiceUpdatedUpdate::class.java)
        )

        try {
            commandGateway.sendAndWait<Any>(
                MarkInvoiceOverdueCommand(
                    invoiceId = InvoiceId.from(invoiceId)
                )
            )

            // Wait for projection update
            subscriptionQuery.updates().blockFirst(Duration.ofSeconds(5))

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    @PostMapping("/{invoiceId}/cancel")
    fun cancelInvoice(
        @PathVariable invoiceId: String,
        @RequestBody request: CancelInvoiceRequest
    ): ResponseEntity<Void> {
        val subscriptionQuery = queryGateway.subscriptionQuery(
            FindAllInvoicesQuery(),
            ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
            ResponseTypes.instanceOf(InvoiceUpdatedUpdate::class.java)
        )

        try {
            commandGateway.sendAndWait<Any>(
                CancelInvoiceCommand(
                    invoiceId = InvoiceId.from(invoiceId),
                    reason = request.reason
                )
            )

            // Wait for projection update
            subscriptionQuery.updates().blockFirst(Duration.ofSeconds(5))

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }
}

data class CancelInvoiceRequest(
    val reason: String
)

data class InvoiceDto(
    val invoiceId: String,
    val customerId: String,
    val customerName: String,
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
    customerName = customerName,
    bookingId = bookingId,
    amount = amount,
    dueDate = dueDate,
    status = status.name,
    isInstallment = isInstallment,
    installmentNumber = installmentNumber,
    paidAt = paidAt
)
