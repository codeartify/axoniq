package ch.fitnesslab.membership.application

import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.membership.domain.PaymentMode
import java.time.LocalDate

data class MembershipSignUpRequest(
    val customerId: CustomerId,
    val productVariantId: ProductVariantId,
    val paymentMode: PaymentMode,
    val startDate: LocalDate
)
