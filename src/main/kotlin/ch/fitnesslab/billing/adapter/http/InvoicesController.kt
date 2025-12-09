package ch.fitnesslab.billing.adapter.http

import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.billing.infrastructure.bexio.BexioInvoiceDto
import ch.fitnesslab.billing.infrastructure.bexio.BexioInvoiceService
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.generated.api.InvoicesApi
import ch.fitnesslab.generated.model.CancelInvoiceRequest
import ch.fitnesslab.generated.model.InvoiceDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/invoices")
class InvoicesController(
    private val bexioInvoiceService: BexioInvoiceService,
) : InvoicesApi {
    @GetMapping
    fun getInvoices(
        @RequestParam(required = false) status: InvoiceStatus?,
    ): ResponseEntity<List<InvoiceDto>> {
        val invoices = bexioInvoiceService.fetchAllInvoices()

        val filtered =
            if (status != null) {
                invoices.filter { bexioInvoiceService.mapBexioStatusToInvoiceStatus(it.kbItemStatusId) == status }
            } else {
                invoices
            }

        return ResponseEntity.ok(filtered.map { it.toDto(bexioInvoiceService) })
    }

    @GetMapping("/customer/{customerId}")
    override fun getInvoicesByCustomerId(
        @PathVariable customerId: String,
    ): ResponseEntity<List<InvoiceDto>> {
        val invoices = bexioInvoiceService.fetchInvoicesByCustomerId(CustomerId.from(customerId))
        return ResponseEntity.ok(invoices.map { it.toDto(bexioInvoiceService) })
    }

    @GetMapping("/{invoiceId}")
    override fun getInvoiceById(
        @PathVariable invoiceId: String,
    ): ResponseEntity<InvoiceDto> {
        // Assuming invoiceId is the Bexio invoice ID
        val invoice =
            bexioInvoiceService.fetchInvoiceById(invoiceId.toInt())
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(invoice.toDto(bexioInvoiceService))
    }

    @PostMapping("/{invoiceId}/pay")
    override fun markAsPaid(
        @PathVariable invoiceId: String,
    ): ResponseEntity<Unit> {
        // Payment status is now managed in Bexio
        // This endpoint would need to call Bexio API to mark invoice as paid
        // For now, return not implemented
        return ResponseEntity.status(501).build()
    }

    @PostMapping("/{invoiceId}/mark-overdue")
    override fun markAsOverdue(
        @PathVariable invoiceId: String,
    ): ResponseEntity<Unit> {
        // Overdue status is now managed in Bexio
        // This endpoint would need to call Bexio API
        return ResponseEntity.status(501).build()
    }

    @PostMapping("/{invoiceId}/cancel")
    override fun cancelInvoice(
        @PathVariable invoiceId: String,
        @RequestBody cancelInvoiceRequest: CancelInvoiceRequest,
    ): ResponseEntity<Unit> {
        // Cancel status is now managed in Bexio
        // This endpoint would need to call Bexio API
        return ResponseEntity.status(501).build()
    }
}

private fun BexioInvoiceDto.toDto(service: BexioInvoiceService) =
    InvoiceDto(
        invoiceId = id.toString(),
        customerId = contactId.toString(),
        customerName = contactAddress?.substringBefore("\n") ?: "Unknown",
        bookingId = apiReference ?: "N/A",
        amount = total,
        dueDate = LocalDate.parse(isValidTo, DateTimeFormatter.ISO_LOCAL_DATE),
        status = service.mapBexioStatusToInvoiceStatus(kbItemStatusId).name,
        isInstallment = false,
        installmentNumber = null,
        paidAt = null, // Would need to parse from Bexio data
    )
