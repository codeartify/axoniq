package ch.fitnesslab.membership.application

import ch.fitnesslab.billing.application.FindAllInvoicesQuery
import ch.fitnesslab.billing.application.InvoiceUpdated
import ch.fitnesslab.billing.application.InvoiceView
import ch.fitnesslab.billing.domain.commands.CreateInvoiceCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoicePaidCommand
import ch.fitnesslab.booking.application.BookingUpdatedUpdate
import ch.fitnesslab.booking.application.BookingView
import ch.fitnesslab.booking.application.FindAllBookingsQuery
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.common.types.*
import ch.fitnesslab.customers.application.FindCustomerByIdQuery
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.membership.domain.DueDate
import ch.fitnesslab.membership.domain.PaymentMode
import ch.fitnesslab.product.application.FindAllProductContractsQuery
import ch.fitnesslab.product.application.FindProductByIdQuery
import ch.fitnesslab.product.application.ProductContractUpdatedUpdate
import ch.fitnesslab.product.application.ProductContractView
import ch.fitnesslab.product.domain.commands.CreateProductContractCommand
import ch.fitnesslab.product.infrastructure.ProductVariantEntity
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MembershipSignUpService(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    fun signUp(request: MembershipSignUpRequest): MembershipSignUpResult {
        val customerId = request.customerId

        ensureCustomerExists(customerId)

        val bookingSubscription = createBookingSubscriptionQuery()
        val contractSubscription = createContractSubscription()
        val invoiceSubscription = createInvoiceSubscriptionQuery()

        val productVariantId = request.productVariantId
        val productVariantEntity = getProductVariant(productVariantId)

        val bookingId = BookingId.generate()
        val contractId = ProductContractId.generate()
        val invoiceId = InvoiceId.generate()

        try {
            // 1. Place booking
            commandGateway.sendAndWait<Any>(
                PlaceBookingCommand(
                    bookingId = bookingId,
                    payerCustomerId = customerId,
                    purchasedProducts = listOf(PurchasedProduct(productVariantId = productVariantId)),
                ),
            )
            waitForUpdateOf(bookingSubscription)

            commandGateway.sendAndWait<Any>(
                CreateProductContractCommand(
                    contractId = contractId,
                    customerId = customerId,
                    productVariantId = productVariantId,
                    bookingId = bookingId,
                    validity = createValidity(request.startDate, (productVariantEntity.durationCount)),
                    sessionsTotal = null,
                ),
            )
            waitForUpdateOf(contractSubscription)

            // 3. Create invoice
            commandGateway.sendAndWait<Any>(
                CreateInvoiceCommand(
                    invoiceId = invoiceId,
                    bookingId = bookingId,
                    customerId = customerId,
                    productVariantId = productVariantId,
                    amount = productVariantEntity.flatRate,
                    dueDate = DueDate.inDays(30),
                    isInstallment = false,
                    installmentNumber = null,
                ),
            )
            waitForUpdateOf(invoiceSubscription)

            if (paidOnSite(request.paymentMode)) {
                commandGateway.sendAndWait<Any>(MarkInvoicePaidCommand(invoiceId = invoiceId))
                waitForUpdateOf(invoiceSubscription)
            }

            return MembershipSignUpResult(
                contractId = contractId,
                bookingId = bookingId,
                invoiceId = invoiceId,
            )
        } finally {
            bookingSubscription.close()
            contractSubscription.close()
            invoiceSubscription.close()
        }
    }


    private fun paidOnSite(mode: PaymentMode): Boolean = mode == PaymentMode.PAY_ON_SITE

    private fun createInvoiceSubscriptionQuery(): SubscriptionQueryResult<MutableList<InvoiceView>, InvoiceUpdated> =
        queryGateway.subscriptionQuery(
            FindAllInvoicesQuery(),
            ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
            ResponseTypes.instanceOf(InvoiceUpdated::class.java),
        )

    private fun createContractSubscription(): SubscriptionQueryResult<MutableList<ProductContractView>, ProductContractUpdatedUpdate> =
        queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java),
        )

    private fun createBookingSubscriptionQuery(): SubscriptionQueryResult<MutableList<BookingView>, BookingUpdatedUpdate> =
        queryGateway.subscriptionQuery(
            FindAllBookingsQuery(),
            ResponseTypes.multipleInstancesOf(BookingView::class.java),
            ResponseTypes.instanceOf(BookingUpdatedUpdate::class.java),
        )

    private fun getProductVariant(productVariantId: ProductVariantId): ProductVariantEntity {
        val productVariant =
            queryGateway
                .query(
                    FindProductByIdQuery(productId = productVariantId),
                    ProductVariantEntity::class.java,
                ).get()

        return when (productVariant) {
            null -> throw IllegalArgumentException("Product not found")
            else -> productVariant
        }
    }

    private fun ensureCustomerExists(customerId: CustomerId) {
        val customer =
            queryGateway
                .query(
                    FindCustomerByIdQuery(customerId = customerId),
                    CustomerEntity::class.java,
                ).get()

        when (customer) {
            null -> throw IllegalArgumentException("Customer not found")
            else -> customer
        }
    }

    private fun createValidity(
        startDate: LocalDate,
        durationInMonths: Int?,
    ): DateRange? {
        val durationInMonths = durationInMonths
        return when {
            durationInMonths != null -> DateRange.toRange(startDate, durationInMonths)
            else -> null
        }
    }
}
