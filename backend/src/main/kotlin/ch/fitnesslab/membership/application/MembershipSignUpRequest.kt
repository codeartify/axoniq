package ch.fitnesslab.membership.application

import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.ProductId
import ch.fitnesslab.membership.domain.PaymentMode
import java.time.LocalDate

data class MembershipSignUpRequest(
    val customerId: CustomerId,
    val productId: ProductId,
    val paymentMode: PaymentMode,
    val startDate: LocalDate,
)
