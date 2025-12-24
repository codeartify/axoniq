package ch.fitnesslab.billing.infrastructure.bexio

import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.customers.application.FindCustomerByIdQuery
import ch.fitnesslab.customers.infrastructure.CustomerEntity
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.product.application.FindProductByIdQuery
import ch.fitnesslab.product.infrastructure.ProductVariantEntity
import org.axonframework.queryhandling.QueryGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class BexioInvoiceService(
    private val bexioClient: BexioClient,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(BexioInvoiceService::class.java)

    fun createInvoiceInBexio(
        invoiceId: InvoiceId,
        customerId: CustomerId,
        productVariantId: ProductVariantId,
        amount: BigDecimal,
        dueDate: LocalDate,
    ): Int {
        // Get customer with Bexio contact ID
        val customer =
            queryGateway
                .query(
                    FindCustomerByIdQuery(customerId = customerId),
                    CustomerEntity::class.java,
                ).get() ?: throw IllegalArgumentException("Customer not found")

        val bexioContactId =
            customer.bexioContactId
                ?: throw IllegalArgumentException("Customer does not have a Bexio contact ID")

        // Get product details
        val product =
            queryGateway
                .query(
                    FindProductByIdQuery(productId = productVariantId),
                    ProductVariantEntity::class.java,
                ).get() ?: throw IllegalArgumentException("Product not found")

        // Create invoice in Bexio
        val request =
            BexioCreateInvoiceRequest(
                contactId = bexioContactId,
                userId = 1, // Required: Bexio user ID - TODO: make configurable
                isValidFrom = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                title = "Membership: ${product.name}",
                positions =
                    listOf(
                        BexioInvoicePosition(
                            text = product.name,
                            unitPrice = amount,
                            accountId = 128, // Revenue account - TODO: Configure with actual Bexio account ID
                            taxId = 14, // Tax rate ID - TODO: Configure with actual Bexio tax ID (e.g., 22 = 8.1% VAT in CH)
                            amount = BigDecimal.ONE,
                        ),
                    ),
                apiReference = invoiceId.value.toString(),
            )

        val bexioInvoice = bexioClient.createInvoice(request)
        logger.info("Created Bexio invoice ${bexioInvoice.id} for internal invoice ${invoiceId.value}")

        return bexioInvoice.id
    }

    fun fetchAllInvoices(): List<BexioInvoiceDto> = bexioClient.fetchInvoices()

    fun fetchInvoicesByCustomerId(customerId: CustomerId): List<BexioInvoiceDto> {
        val customer =
            queryGateway
                .query(
                    FindCustomerByIdQuery(customerId = customerId),
                    CustomerEntity::class.java,
                ).get() ?: return emptyList()

        val bexioContactId = customer.bexioContactId ?: return emptyList()

        return bexioClient.fetchInvoicesByContactId(bexioContactId)
    }

    fun fetchInvoiceById(bexioInvoiceId: Int): BexioInvoiceDto? = bexioClient.fetchInvoiceById(bexioInvoiceId)

    companion object {
        fun mapBexioStatusToInvoiceStatus(kbItemStatusId: Int): InvoiceStatus =
            when (kbItemStatusId) {
                7 -> InvoiceStatus.OPEN // Draft
                9 -> InvoiceStatus.OPEN // Pending
                19 -> InvoiceStatus.PAID // Paid
                18 -> InvoiceStatus.CANCELLED // Cancelled
                else -> InvoiceStatus.OPEN
            }
    }
}
