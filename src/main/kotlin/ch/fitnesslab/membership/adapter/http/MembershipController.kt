package ch.fitnesslab.membership.adapter.http

import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.membership.application.MembershipSignUpRequest
import ch.fitnesslab.membership.application.MembershipSignUpResult
import ch.fitnesslab.membership.application.MembershipSignUpService
import ch.fitnesslab.membership.application.PaymentMode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/memberships")
class MembershipController(
    private val membershipSignUpService: MembershipSignUpService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: MembershipSignUpRequestDto): ResponseEntity<MembershipSignUpResultDto> {
        val signUpRequest = MembershipSignUpRequest(
            customerId = CustomerId.from(request.customerId),
            customerName = request.customerName,
            customerEmail = request.customerEmail,
            productVariantId = ProductVariantId.from(request.productVariantId),
            price = request.price,
            durationMonths = request.durationMonths,
            paymentMode = request.paymentMode
        )

        val result = membershipSignUpService.signUp(
            signUpRequest
        )

        return ResponseEntity.ok(
            MembershipSignUpResultDto(
                contractId = result.contractId.toString(),
                bookingId = result.bookingId.toString(),
                invoiceId = result.invoiceId.toString()
            )
        )
    }
}

data class MembershipSignUpRequestDto(
    val customerId: String,
    val customerName: String,
    val customerEmail: String,
    val productVariantId: String,
    val price: BigDecimal,
    val durationMonths: Int,
    val paymentMode: PaymentMode
)

data class MembershipSignUpResultDto(
    val contractId: String,
    val bookingId: String,
    val invoiceId: String
)
