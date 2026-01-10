package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.membership.domain.DueDate
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal

data class CreateInvoiceCommand(
    @TargetAggregateIdentifier
    val invoiceId: InvoiceId,
    val bookingId: BookingId,
    val customerId: CustomerId,
    val productVariantId: ProductVariantId?,
    val amount: BigDecimal,
    val dueDate: DueDate,
    val isInstallment: Boolean = false,
    val installmentNumber: Int? = null,
)
