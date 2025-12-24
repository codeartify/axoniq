package ch.fitnesslab.membership.adapter.http

import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.generated.api.MembershipsApi
import ch.fitnesslab.generated.model.MembershipSignUpRequestDto
import ch.fitnesslab.generated.model.MembershipSignUpResultDto
import ch.fitnesslab.membership.application.MembershipSignUpRequest
import ch.fitnesslab.membership.application.MembershipSignUpService
import ch.fitnesslab.membership.domain.PaymentMode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class MembershipsController(
    private val membershipSignUpService: MembershipSignUpService,
) : MembershipsApi {
    override fun signUp(membershipSignUpRequestDto: MembershipSignUpRequestDto): ResponseEntity<MembershipSignUpResultDto> {
        val result =
            membershipSignUpService.signUp(
                MembershipSignUpRequest(
                    customerId = CustomerId.from(membershipSignUpRequestDto.customerId),
                    productVariantId = ProductVariantId.from(membershipSignUpRequestDto.productVariantId),
                    paymentMode = membershipSignUpRequestDto.paymentMode.let { PaymentMode.valueOf(it.name) },
                    startDate = LocalDate.from(membershipSignUpRequestDto.startDate),
                ),
            )

        return ResponseEntity.ok(
            MembershipSignUpResultDto(
                contractId = result.contractId.toString(),
                bookingId = result.bookingId.toString(),
                invoiceId = result.invoiceId.toString(),
                bexioInvoiceId = result.bexioInvoiceId,
            ),
        )
    }
}
