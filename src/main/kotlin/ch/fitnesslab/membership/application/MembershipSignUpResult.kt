package ch.fitnesslab.membership.application

import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.InvoiceId
import ch.fitnesslab.common.types.ProductContractId

data class MembershipSignUpResult(
    val contractId: ProductContractId,
    val bookingId: BookingId,
    val invoiceId: InvoiceId,
    val bexioInvoiceId: Int,
)
