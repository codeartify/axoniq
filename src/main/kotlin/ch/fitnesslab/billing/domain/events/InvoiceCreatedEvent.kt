package ch.fitnesslab.billing.domain.events

import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.InvoiceId
import ch.fitnesslab.common.types.ProductVariantId
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
