package ch.fitnesslab.membership.application

import ch.fitnesslab.booking.domain.Participant
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.billing.domain.commands.CreateInvoiceCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoicePaidCommand
import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.InvoiceId
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.commands.CreateProductContractCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class MembershipSignUpService(
    private val commandGateway: CommandGateway
) {

    fun signUp(request: MembershipSignUpRequest): MembershipSignUpResult {
        val bookingId = BookingId.generate()
        val contractId = ProductContractId.generate()
        val invoiceId = InvoiceId.generate()

        // 1. Place booking
        commandGateway.sendAndWait<Any>(
            PlaceBookingCommand(
                bookingId = bookingId,
                payerCustomerId = request.customerId,
                purchasedProducts = listOf(
                    PurchasedProduct(
                        productVariantId = request.productVariantId,
                        participants = listOf(
                            Participant(
                                displayName = request.customerName,
                                email = request.customerEmail
                            )
                        ),
                        totalPrice = request.price
                    )
                )
            )
        )

        // 2. Create product contract (ACTIVE immediately for membership)
        val validity = DateRange(
            start = LocalDate.now(),
            end = LocalDate.now().plusMonths(request.durationMonths.toLong())
        )

        commandGateway.sendAndWait<Any>(
            CreateProductContractCommand(
                contractId = contractId,
                customerId = request.customerId,
                productVariantId = request.productVariantId,
                bookingId = bookingId,
                validity = validity,
                sessionsTotal = null
            )
        )

        // 3. Create invoice
        commandGateway.sendAndWait<Any>(
            CreateInvoiceCommand(
                invoiceId = invoiceId,
                bookingId = bookingId,
                customerId = request.customerId,
                productVariantId = request.productVariantId,
                amount = request.price,
                dueDate = LocalDate.now().plusDays(30),
                isInstallment = false,
                installmentNumber = null
            )
        )

        // 4. If PAY_ON_SITE, mark as paid immediately
        if (request.paymentMode == PaymentMode.PAY_ON_SITE) {
            commandGateway.sendAndWait<Any>(
                MarkInvoicePaidCommand(invoiceId = invoiceId)
            )
        }

        return MembershipSignUpResult(
            contractId = contractId,
            bookingId = bookingId,
            invoiceId = invoiceId
        )
    }
}

data class MembershipSignUpRequest(
    val customerId: CustomerId,
    val customerName: String,
    val customerEmail: String,
    val productVariantId: ProductVariantId,
    val price: BigDecimal,
    val durationMonths: Int,
    val paymentMode: PaymentMode
)

enum class PaymentMode {
    PAY_ON_SITE,
    INVOICE_EMAIL
}

data class MembershipSignUpResult(
    val contractId: ProductContractId,
    val bookingId: BookingId,
    val invoiceId: InvoiceId
)
