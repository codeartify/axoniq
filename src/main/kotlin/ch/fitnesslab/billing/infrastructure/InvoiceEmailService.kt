package ch.fitnesslab.billing.infrastructure

import ch.fitnesslab.billing.application.InvoiceView
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class InvoiceEmailService(
    private val mailSender: JavaMailSender,
    private val pdfGenerator: InvoicePdfGenerator,
) {
    fun sendInvoiceEmail(
        invoice: InvoiceView,
        customerName: String,
        customerEmail: String,
    ) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setFrom("noreply@fitnesslab.com")
            helper.setTo(customerEmail)
            helper.setSubject("Your Invoice from FitnessLab - ${invoice.invoiceId}")

            val emailBody = buildEmailBody(invoice, customerName)
            helper.setText(emailBody, false)

            // Generate and attach PDF
            val pdfBytes = pdfGenerator.generateInvoicePdf(invoice, customerName, customerEmail)
            helper.addAttachment("Invoice-${invoice.invoiceId}.pdf", { pdfBytes.inputStream() }, "application/pdf")

            mailSender.send(message)

            println("Invoice email sent successfully to $customerEmail for invoice ${invoice.invoiceId}")
        } catch (e: Exception) {
            println("Failed to send invoice email to $customerEmail: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun buildEmailBody(
        invoice: InvoiceView,
        customerName: String,
    ): String =
        """
        Dear $customerName,
        
        Thank you for your business with FitnessLab!
        
        Please find attached your invoice details:
        
        Invoice Number: ${invoice.invoiceId}
        Amount: $${invoice.amount}
        Due Date: ${invoice.dueDate.format(DateTimeFormatter.ISO_DATE)}
        Status: ${invoice.status}
        
        Please ensure payment is made by the due date.
        
        If you have any questions, please don't hesitate to contact us.
        
        Best regards,
        FitnessLab Team
        """.trimIndent()
}
