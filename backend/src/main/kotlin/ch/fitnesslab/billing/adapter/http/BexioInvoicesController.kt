// package ch.fitnesslab.billing.adapter.http
//
// import ch.fitnesslab.billing.domain.InvoiceStatus
// import ch.fitnesslab.billing.infrastructure.bexio.BexioInvoiceDto
// import ch.fitnesslab.billing.infrastructure.bexio.BexioInvoiceService
// import ch.fitnesslab.customers.infrastructure.bexio.BexioContactService
// import ch.fitnesslab.domain.value.CustomerId
// import ch.fitnesslab.generated.api.InvoicesApi
// import ch.fitnesslab.generated.model.CancelInvoiceRequest
// import ch.fitnesslab.generated.model.InvoiceDto
// import org.springframework.http.ResponseEntity
// import org.springframework.web.bind.annotation.RestController
// import java.time.LocalDate
// import java.time.format.DateTimeFormatter
//
// @RestController
// class BexioInvoicesController(
//    private val bexioInvoiceService: BexioInvoiceService,
//    private val bexioContactService: BexioContactService,
// ) : InvoicesApi {
//    override fun getInvoices(status: String?): ResponseEntity<List<InvoiceDto>> {
//        val invoices = bexioInvoiceService.fetchAllInvoices()
//        val filtered =
//            if (status != null) {
//                invoices.filter {
//                    BexioInvoiceService.mapBexioStatusToInvoiceStatus(it.kbItemStatusId) ==
//                        InvoiceStatus.valueOf(
//                            status,
//                        )
//                }
//            } else {
//                invoices
//            }
//
//        return ResponseEntity.ok(filtered.map { toDto(it) })
//    }
//
//    override fun getInvoicesByCustomerId(customerId: String): ResponseEntity<List<InvoiceDto>> {
//        val invoices = bexioInvoiceService.fetchInvoicesByCustomerId(CustomerId.from(customerId))
//        return ResponseEntity.ok(invoices.map { toDto(it) })
//    }
//
//    override fun getInvoiceById(invoiceId: String): ResponseEntity<InvoiceDto> {
//        // Assuming invoiceId is the Bexio invoice ID
//        val invoice =
//            bexioInvoiceService.fetchInvoiceById(invoiceId.toInt())
//                ?: return ResponseEntity.notFound().build()
//
//        return ResponseEntity.ok(toDto(invoice))
//    }
//
//    override fun markAsPaid(invoiceId: String): ResponseEntity<Unit> {
//        // Payment status is now managed in Bexio
//        // This endpoint would need to call Bexio API to mark invoice as paid
//        // For now, return not implemented
//        return ResponseEntity.status(501).build()
//    }
//
//    override fun markAsOverdue(invoiceId: String): ResponseEntity<Unit> {
//        // Overdue status is now managed in Bexio
//        // This endpoint would need to call Bexio API
//        return ResponseEntity.status(501).build()
//    }
//
//    override fun cancelInvoice(
//        invoiceId: String,
//        cancelInvoiceRequest: CancelInvoiceRequest,
//    ): ResponseEntity<Unit> {
//        // Cancel status is now managed in Bexio
//        // This endpoint would need to call Bexio API
//        return ResponseEntity.status(501).build()
//    }
//
//    private fun toDto(invoiceDto: BexioInvoiceDto): InvoiceDto {
//        val contact = bexioContactService.fetchContact(invoiceDto.contactId)
//
//        val customerName: String
//        if (contact == null) {
//            customerName = invoiceDto.contactAddress?.substringBefore("\n") ?: "Unknown"
//        } else {
//            customerName = "${contact.name1}  ${contact.name2 ?: ""}".trim()
//        }
//
//        return InvoiceDto(
//            invoiceId = invoiceDto.id.toString(),
//            customerId = invoiceDto.contactId.toString(),
//            customerName = customerName,
//            bookingId = invoiceDto.apiReference ?: "N/A",
//            amount = invoiceDto.total,
//            dueDate = LocalDate.parse(invoiceDto.isValidTo, DateTimeFormatter.ISO_LOCAL_DATE),
//            status = BexioInvoiceService.mapBexioStatusToInvoiceStatus(invoiceDto.kbItemStatusId).name,
//            isInstallment = false,
//            installmentNumber = null,
//            paidAt = null, // Would need to parse from Bexio data
//        )
//    }
// }
