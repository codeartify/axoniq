package ch.fitnesslab.membership.application

import ch.fitnesslab.billing.infrastructure.bexio.BexioInvoiceService
import ch.fitnesslab.booking.application.BookingUpdatedUpdate
import ch.fitnesslab.booking.application.BookingView
import ch.fitnesslab.booking.application.FindAllBookingsQuery
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.customers.application.FindCustomerByIdQuery
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.domain.value.BexioContactId
import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.domain.value.ProductContractId
import ch.fitnesslab.domain.value.ProductVariantId
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
    private val bexioInvoiceService: BexioInvoiceService,
) {
    fun signUp(request: MembershipSignUpRequest): MembershipSignUpResult {
        val customerId = request.customerId

        ensureCustomerExists(customerId)

        val bookingSubscription = createBookingSubscriptionQuery()
        val contractSubscription = createContractSubscription()

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

            // 2. Create contract
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

            // 3. Create invoice in Bexio (currently doesn't work)
            val dueDate = LocalDate.now().plusDays(30)
//            val bexioInvoiceId =
//                bexioInvoiceService.createInvoiceInBexio(
//                    invoiceId = invoiceId,
//                    customerId = customerId,
//                    productVariantId = productVariantId,
//                    amount = productVariantEntity.flatRate,
//                    dueDate = dueDate,
//                )

            // Note: Payment status is now managed in Bexio
            // The payment mode (PAY_ON_SITE) would need to be handled in Bexio separately

            return MembershipSignUpResult(
                contractId = contractId,
                bookingId = bookingId,
                invoiceId = invoiceId,
                bexioInvoiceId = 0,
            )
        } finally {
            bookingSubscription.close()
            contractSubscription.close()
        }
    }

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

        if (customer == null) {
            throw IllegalArgumentException("Customer not found")
        }
    }

    private fun createValidity(
        startDate: LocalDate,
        durationInMonths: Int?,
    ): DateRange? =
        when {
            durationInMonths != null -> DateRange.toRange(startDate, durationInMonths)
            else -> null
        }
}
