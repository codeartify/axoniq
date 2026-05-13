package ch.fitnesslab.membership.application

import ch.fitnesslab.billing.application.FindAllInvoicesQuery
import ch.fitnesslab.billing.domain.commands.CreateInvoiceCommand
import ch.fitnesslab.booking.application.FindAllBookingsQuery
import ch.fitnesslab.booking.domain.PurchasedProduct
import ch.fitnesslab.booking.domain.commands.PlaceBookingCommand
import ch.fitnesslab.contract.application.FindAllContractsQuery
import ch.fitnesslab.contract.domain.commands.CreateContractCommand
import ch.fitnesslab.customers.application.FindCustomerByIdQuery
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.domain.value.*
import ch.fitnesslab.membership.domain.DueDate
import ch.fitnesslab.product.application.FindProductByIdQuery
import ch.fitnesslab.product.infrastructure.ProductVariantEntity
import ch.fitnesslab.utils.waitForUpdatesOf
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.queryhandling.gateway.QueryGateway
import org.reactivestreams.Publisher
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
            waitForUpdatesOf(
                listOf(
                    bookingSubscription,
                    contractSubscription,
                    invoiceSubscription,
                ),
            ) {
                commandGateway.sendAndWait(
                    PlaceBookingCommand(
                        bookingId = bookingId,
                        payerCustomerId = customerId,
                        purchasedProducts = listOf(PurchasedProduct(productId = productId)),
                    ),
                )

                // 2. Create contract
                commandGateway.sendAndWait(
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
                commandGateway.sendAndWait(
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
            }

            return MembershipSignUpResult(
                contractId = contractId,
                bookingId = bookingId,
                invoiceId = invoiceId,
            )
        } finally {
        }
    }

    private fun createInvoiceSubscription(): Publisher<Any> =
        queryGateway.subscriptionQuery(
            FindAllInvoicesQuery(),
            Any::class.java,
        )

    private fun createContractSubscription(): Publisher<Any> =
        queryGateway.subscriptionQuery(
            FindAllContractsQuery(),
            Any::class.java,
        )

    private fun createBookingSubscriptionQuery(): Publisher<Any> =
        queryGateway.subscriptionQuery(
            FindAllBookingsQuery(),
            Any::class.java,
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
