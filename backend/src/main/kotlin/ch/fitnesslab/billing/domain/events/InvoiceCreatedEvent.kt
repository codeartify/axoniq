package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.membership.domain.DueDate
import org.axonframework.serialization.Revision
import java.math.BigDecimal

@Revision("1.0")
data class InvoiceCreatedEvent(
    val invoiceId: InvoiceId,
    val bookingId: BookingId,
    val customerId: CustomerId,
    val productVariantId: ProductVariantId?,
    val amount: BigDecimal,
    val dueDate: DueDate,
    val status: InvoiceStatus,
    val isInstallment: Boolean,
    val installmentNumber: Int?,
)
