package ch.fitnesslab.billing.domain.commands

import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.InvoiceId
import ch.fitnesslab.common.types.ProductVariantId
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
