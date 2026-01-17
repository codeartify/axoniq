package ch.fitnesslab.billing.adapter.http

import ch.fitnesslab.billing.application.FindAllInvoicesQuery
import ch.fitnesslab.billing.application.InvoiceProjection
import ch.fitnesslab.billing.application.InvoiceUpdated
import ch.fitnesslab.billing.application.InvoiceView
import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.billing.domain.commands.CancelInvoiceCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoiceOverdueCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoicePaidCommand
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.generated.api.InvoicesApi
import ch.fitnesslab.generated.model.CancelInvoiceRequest
import ch.fitnesslab.generated.model.InvoiceDto
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@RestController
@RequestMapping("/api/invoices")
class InvoicesController(
    private val invoiceProjection: InvoiceProjection,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) : InvoicesApi {
    @GetMapping
    fun getInvoices(
        @RequestParam(required = false) status: InvoiceStatus?,
    ): ResponseEntity<List<InvoiceDto>> {
        val invoices =
            if (status != null) {
                invoiceProjection.findByStatus(status)
            } else {
                invoiceProjection.findAll()
            }

        return ResponseEntity.ok(invoices.map { it.toDto() })
    }

    @GetMapping("/customer/{customerId}")
    override fun getInvoicesByCustomerId(
        @PathVariable customerId: String,
    ): ResponseEntity<List<InvoiceDto>> {
        val invoices = invoiceProjection.findByCustomerId(customerId)
        return ResponseEntity.ok(invoices.map { it.toDto() })
    }

    @GetMapping("/{invoiceId}")
    override fun getInvoiceById(
        @PathVariable invoiceId: String,
    ): ResponseEntity<InvoiceDto> {
        val invoice =
            invoiceProjection.findById(InvoiceId.from(invoiceId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(invoice.toDto())
    }

    @PostMapping("/{invoiceId}/pay")
    override fun markAsPaid(
        @PathVariable invoiceId: String,
    ): ResponseEntity<Unit> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllInvoicesQuery(),
                ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
                ResponseTypes.instanceOf(InvoiceUpdated::class.java),
            )

        try {
            commandGateway.sendAndWait<Any>(
                MarkInvoicePaidCommand(
                    invoiceId = InvoiceId.from(invoiceId),
                    paidAt = Instant.now(),
                ),
            )

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    @PostMapping("/{invoiceId}/mark-overdue")
    override fun markAsOverdue(
        @PathVariable invoiceId: String,
    ): ResponseEntity<Unit> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllInvoicesQuery(),
                ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
                ResponseTypes.instanceOf(InvoiceUpdated::class.java),
            )

        try {
            commandGateway.sendAndWait<Any>(
                MarkInvoiceOverdueCommand(
                    invoiceId = InvoiceId.from(invoiceId),
                ),
            )

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    @PostMapping("/{invoiceId}/cancel")
    override fun cancelInvoice(
        @PathVariable invoiceId: String,
        @RequestBody cancelInvoiceRequest: CancelInvoiceRequest,
    ): ResponseEntity<Unit> {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllInvoicesQuery(),
                ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
                ResponseTypes.instanceOf(InvoiceUpdated::class.java),
            )

        try {
            commandGateway.sendAndWait<Any>(
                CancelInvoiceCommand(
                    invoiceId = InvoiceId.from(invoiceId),
                    reason = cancelInvoiceRequest.reason,
                ),
            )

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }
}

private fun InvoiceView.toDto() =
    InvoiceDto(
        invoiceId = invoiceId,
        customerId = customerId,
        customerName = customerName,
        bookingId = bookingId,
        amount = amount,
        dueDate = dueDate,
        status = status.name,
        isInstallment = isInstallment,
        installmentNumber = installmentNumber,
        paidAt = paidAt?.let { OffsetDateTime.ofInstant(it, ZoneId.systemDefault()) },
    )
