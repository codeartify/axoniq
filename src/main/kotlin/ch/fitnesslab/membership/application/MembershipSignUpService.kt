package ch.fitnesslab.membership.application

import ch.fitnesslab.billing.application.FindAllInvoicesQuery
import ch.fitnesslab.billing.application.InvoiceUpdatedUpdate
import ch.fitnesslab.billing.application.InvoiceView
import ch.fitnesslab.billing.domain.commands.CreateInvoiceCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoicePaidCommand
import ch.fitnesslab.booking.application.BookingUpdatedUpdate
import ch.fitnesslab.booking.application.BookingView
import ch.fitnesslab.booking.application.FindAllBookingsQuery
import ch.fitnesslab.booking.domain.Participant
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.common.types.*
import ch.fitnesslab.customers.application.CustomerUpdatedUpdate
import ch.fitnesslab.customers.application.CustomerView
import ch.fitnesslab.customers.application.FindAllCustomersQuery
import ch.fitnesslab.product.application.FindAllProductContractsQuery
import ch.fitnesslab.product.application.ProductContractUpdatedUpdate
import ch.fitnesslab.product.application.ProductContractView
import ch.fitnesslab.product.domain.commands.CreateProductContractCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate

@Service
class MembershipSignUpService(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    fun signUp(request: MembershipSignUpRequest): MembershipSignUpResult {
        val bookingId = BookingId.generate()
        val contractId = ProductContractId.generate()
        val invoiceId = InvoiceId.generate()

        // Create subscription queries for all projections that will be updated
        val bookingSubscription = queryGateway.subscriptionQuery(
            FindAllBookingsQuery(),
            ResponseTypes.multipleInstancesOf(BookingView::class.java),
            ResponseTypes.instanceOf(BookingUpdatedUpdate::class.java)
        )

        val contractSubscription = queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java)
        )

        val invoiceSubscription = queryGateway.subscriptionQuery(
            FindAllInvoicesQuery(),
            ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
            ResponseTypes.instanceOf(InvoiceUpdatedUpdate::class.java)
        )

        try {
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
            // Wait for booking projection update
            bookingSubscription.updates().blockFirst(Duration.ofSeconds(5))

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
            // Wait for contract projection update
            contractSubscription.updates().blockFirst(Duration.ofSeconds(5))

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
            // Wait for invoice projection update
            invoiceSubscription.updates().blockFirst(Duration.ofSeconds(5))

            // 4. If PAY_ON_SITE, mark as paid immediately
            if (request.paymentMode == PaymentMode.PAY_ON_SITE) {
                commandGateway.sendAndWait<Any>(
                    MarkInvoicePaidCommand(invoiceId = invoiceId)
                )
                // Wait for invoice paid update
                invoiceSubscription.updates().blockFirst(Duration.ofSeconds(5))
            }

            return MembershipSignUpResult(
                contractId = contractId,
                bookingId = bookingId,
                invoiceId = invoiceId
            )
        } finally {
            bookingSubscription.close()
            contractSubscription.close()
            invoiceSubscription.close()
        }
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
