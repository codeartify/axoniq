package ch.fitnesslab.membership.adapter.http

import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.generated.api.MembershipsApi
import ch.fitnesslab.generated.model.MembershipSignUpRequestDto
import ch.fitnesslab.generated.model.MembershipSignUpResultDto
import ch.fitnesslab.membership.application.MembershipSignUpRequest
import ch.fitnesslab.membership.application.MembershipSignUpService
import ch.fitnesslab.membership.application.PaymentMode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/memberships")
class MembershipsController(
    private val membershipSignUpService: MembershipSignUpService,
) : MembershipsApi {
    @PostMapping("/sign-up")
    override fun signUp(
        @RequestBody membershipSignUpRequestDto: MembershipSignUpRequestDto,
    ): ResponseEntity<MembershipSignUpResultDto> {
        val signUpRequest =
            MembershipSignUpRequest(
                customerId = CustomerId.from(membershipSignUpRequestDto.customerId),
                customerName = membershipSignUpRequestDto.customerName,
                customerEmail = membershipSignUpRequestDto.customerEmail,
                productVariantId = ProductVariantId.from(membershipSignUpRequestDto.productVariantId),
                price = membershipSignUpRequestDto.price,
                durationMonths = membershipSignUpRequestDto.durationMonths,
                paymentMode = membershipSignUpRequestDto.paymentMode.let { PaymentMode.valueOf(it.name) },
            )

        val result =
            membershipSignUpService.signUp(
                signUpRequest,
            )

        return ResponseEntity.ok(
            MembershipSignUpResultDto(
                contractId = result.contractId.toString(),
                bookingId = result.bookingId.toString(),
                invoiceId = result.invoiceId.toString(),
            ),
        )
    }
}
