package ch.fitnesslab.billing.infrastructure

import ch.fitnesslab.billing.application.InvoiceView
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

@Service
class InvoicePdfGenerator {

    fun generateInvoicePdf(invoice: InvoiceView, customerName: String, customerEmail: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        // Header
        document.add(
            Paragraph("INVOICE")
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )

        document.add(Paragraph("\n"))

        // Company Info
        document.add(Paragraph("FitnessLab").setFontSize(16f).setBold())
        document.add(Paragraph("Fitness Center"))
        document.add(Paragraph("\n"))

        // Invoice Details
        document.add(Paragraph("Invoice Number: ${invoice.invoiceId}").setFontSize(12f))
        document.add(Paragraph("Invoice Date: ${invoice.dueDate.minusDays(30).format(DateTimeFormatter.ISO_DATE)}"))
        document.add(Paragraph("Due Date: ${invoice.dueDate.format(DateTimeFormatter.ISO_DATE)}"))
        document.add(Paragraph("Status: ${invoice.status}"))
        document.add(Paragraph("\n"))

        // Customer Details
        document.add(Paragraph("Bill To:").setBold())
        document.add(Paragraph(customerName))
        document.add(Paragraph(customerEmail))
        document.add(Paragraph("\n"))

        // Invoice Items Table
        val table = Table(floatArrayOf(3f, 1f))
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100f))

        // Table Header
        table.addHeaderCell(Paragraph("Description").setBold())
        table.addHeaderCell(Paragraph("Amount").setBold())

        // Table Data
        val description = if (invoice.isInstallment) {
            "Installment ${invoice.installmentNumber ?: 1} - Membership Fee"
        } else {
            "Membership Fee"
        }
        table.addCell(Paragraph(description))
        table.addCell(Paragraph("$${invoice.amount}").setTextAlignment(TextAlignment.RIGHT))

        document.add(table)
        document.add(Paragraph("\n"))

        // Total
        document.add(
            Paragraph("Total Amount Due: $${invoice.amount}")
                .setFontSize(14f)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
        )

        document.add(Paragraph("\n\n"))
        document.add(Paragraph("Thank you for your business!").setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("Please pay by the due date.").setTextAlignment(TextAlignment.CENTER))

        document.close()

        return outputStream.toByteArray()
    }
}
