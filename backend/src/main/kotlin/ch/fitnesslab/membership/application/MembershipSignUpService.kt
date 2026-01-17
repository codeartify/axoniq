package ch.fitnesslab.membership.application

import ch.fitnesslab.billing.application.FindAllInvoicesQuery
import ch.fitnesslab.billing.application.InvoiceUpdated
import ch.fitnesslab.billing.application.InvoiceView
import ch.fitnesslab.billing.domain.commands.CreateInvoiceCommand
import ch.fitnesslab.booking.application.BookingUpdatedUpdate
import ch.fitnesslab.booking.application.BookingView
import ch.fitnesslab.booking.application.FindAllBookingsQuery
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.contract.application.ContractUpdatedUpdate
import ch.fitnesslab.contract.application.ContractView
import ch.fitnesslab.contract.application.FindAllContractsQuery
import ch.fitnesslab.contract.domain.commands.CreateContractCommand
import ch.fitnesslab.customers.application.FindCustomerByIdQuery
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.domain.value.*
import ch.fitnesslab.membership.domain.DueDate
import ch.fitnesslab.product.application.FindProductByIdQuery
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
        val invoiceSubscription = createInvoiceSubscription()

        val productId = request.productId
        val productVariantEntity = getProductVariant(productId)

        val bookingId = BookingId.generate()
        val contractId = ContractId.generate()
        val invoiceId = InvoiceId.generate()



        try {
            commandGateway.send<Any>(
                PlaceBookingCommand(
                    bookingId = bookingId,
                    payerCustomerId = customerId,
                    purchasedProducts = listOf(PurchasedProduct(productId = productId)),
                ),
            )

            // 2. Create contract
            commandGateway.send<Any>(
                CreateContractCommand(
                    contractId = contractId,
                    customerId = customerId,
                    productId = productId,
                    bookingId = bookingId,
                    validity = createValidity(request.startDate, (productVariantEntity.durationCount)),
                    sessionsTotal = null,
                ),
            )


            val dueDate = DueDate.inDays(30)
            commandGateway.send<Any>(
                CreateInvoiceCommand(
                    invoiceId = invoiceId,
                    bookingId = bookingId,
                    customerId = customerId,
                    productId = productId,
                    amount = productVariantEntity.flatRate,
                    dueDate = dueDate,
                    isInstallment = false,
                    installmentNumber = null,
                ),
            )

            waitForUpdateOf(bookingSubscription)
            waitForUpdateOf(contractSubscription)
            waitForUpdateOf(invoiceSubscription)

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

    private fun createInvoiceSubscription(): SubscriptionQueryResult<MutableList<InvoiceView>, InvoiceUpdated> =
        queryGateway.subscriptionQuery(
            FindAllInvoicesQuery(),
            ResponseTypes.multipleInstancesOf(InvoiceView::class.java),
            ResponseTypes.instanceOf(InvoiceUpdated::class.java),
        )

    private fun createContractSubscription(): SubscriptionQueryResult<MutableList<ContractView>, ContractUpdatedUpdate> =
        queryGateway.subscriptionQuery(
            FindAllContractsQuery(),
            ResponseTypes.multipleInstancesOf(ContractView::class.java),
            ResponseTypes.instanceOf(ContractUpdatedUpdate::class.java),
        )

    private fun createBookingSubscriptionQuery(): SubscriptionQueryResult<MutableList<BookingView>, BookingUpdatedUpdate> =
        queryGateway.subscriptionQuery(
            FindAllBookingsQuery(),
            ResponseTypes.multipleInstancesOf(BookingView::class.java),
            ResponseTypes.instanceOf(BookingUpdatedUpdate::class.java),
        )

    private fun getProductVariant(productId: ProductId): ProductVariantEntity {
        val productVariant =
            queryGateway
                .query(
                    FindProductByIdQuery(productId = productId),
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
