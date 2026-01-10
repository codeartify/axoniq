package ch.fitnesslab.membership.application

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.domain.value.ProductContractId

data class MembershipSignUpResult(
    val contractId: ProductContractId,
    val bookingId: BookingId,
    val invoiceId: InvoiceId,
    val bexioInvoiceId: Int,
)
