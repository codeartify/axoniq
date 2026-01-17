package ch.fitnesslab.membership.application

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.domain.value.ContractId

data class MembershipSignUpResult(
    val contractId: ContractId,
    val bookingId: BookingId,
    val invoiceId: InvoiceId,
    val bexioInvoiceId: Int,
)
