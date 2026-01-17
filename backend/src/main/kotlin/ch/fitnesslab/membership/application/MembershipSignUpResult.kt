package ch.fitnesslab.membership.application

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.InvoiceId

data class MembershipSignUpResult(
    val contractId: ContractId,
    val bookingId: BookingId,
    val invoiceId: InvoiceId,
)
