package ch.fitnesslab.billing.application

import ch.fitnesslab.billing.domain.InvoiceStatus
import ch.fitnesslab.billing.domain.events.InvoiceCancelledEvent
import ch.fitnesslab.billing.domain.events.InvoiceCreatedEvent
import ch.fitnesslab.billing.domain.events.InvoiceMarkedOverdueEvent
import ch.fitnesslab.billing.domain.events.InvoicePaidEvent
import ch.fitnesslab.billing.infrastructure.InvoiceEmailService
import ch.fitnesslab.billing.infrastructure.InvoiceEntity
import ch.fitnesslab.billing.infrastructure.InvoiceRepository
import ch.fitnesslab.customers.application.CustomerProjection
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.InvoiceId
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Component
@ProcessingGroup("invoices")
class InvoiceProjection(
    private val invoiceRepository: InvoiceRepository,
    private val invoiceEmailService: InvoiceEmailService,
    private val customerProjection: CustomerProjection,
    private val queryUpdateEmitter: QueryUpdateEmitter,
) {
    @EventHandler
    fun on(event: InvoiceCreatedEvent) {
        val entity =
            InvoiceEntity(
                invoiceId = event.invoiceId.value,
                customerId = event.customerId.value,
                bookingId = event.bookingId.value,
                productVariantId = event.productId?.value,
                amount = event.amount,
                dueDate = event.dueDate.value,
                status = event.status,
                isInstallment = event.isInstallment,
                installmentNumber = event.installmentNumber,
                paidAt = null,
            )

        invoiceRepository.save(entity)

        // Generate PDF and send email to customer
        try {
            val customer = customerProjection.findById(event.customerId)
            if (customer != null) {
                val customerName = "${customer.salutation} ${customer.firstName} ${customer.lastName}"
                invoiceEmailService.sendInvoiceEmail(entity.toInvoiceView(), customerName, customer.email)
            }
        } catch (e: Exception) {
            println("Failed to send invoice email for invoice ${event.invoiceId}: ${e.message}")
        }

        queryUpdateEmitter.emit(
            FindAllInvoicesQuery::class.java,
            { true },
            InvoiceUpdated(event.invoiceId.value.toString()),
        )
    }

    @EventHandler
    fun on(event: InvoicePaidEvent) {
        invoiceRepository.findById(event.invoiceId.value).ifPresent { existing ->
            val updated =
                InvoiceEntity(
                    invoiceId = existing.invoiceId,
                    customerId = existing.customerId,
                    bookingId = existing.bookingId,
                    productVariantId = existing.productVariantId,
                    amount = existing.amount,
                    dueDate = existing.dueDate,
                    status = InvoiceStatus.PAID,
                    isInstallment = existing.isInstallment,
                    installmentNumber = existing.installmentNumber,
                    paidAt = event.paidAt,
                )
            invoiceRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllInvoicesQuery::class.java,
                { true },
                InvoiceUpdated(event.invoiceId.value.toString()),
            )
        }
    }

    @EventHandler
    fun on(event: InvoiceMarkedOverdueEvent) {
        invoiceRepository.findById(event.invoiceId.value).ifPresent { existing ->
            val updated =
                InvoiceEntity(
                    invoiceId = existing.invoiceId,
                    customerId = existing.customerId,
                    bookingId = existing.bookingId,
                    productVariantId = existing.productVariantId,
                    amount = existing.amount,
                    dueDate = existing.dueDate,
                    status = InvoiceStatus.OVERDUE,
                    isInstallment = existing.isInstallment,
                    installmentNumber = existing.installmentNumber,
                    paidAt = existing.paidAt,
                )
            invoiceRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllInvoicesQuery::class.java,
                { true },
                InvoiceUpdated(event.invoiceId.value.toString()),
            )
        }
    }

    @EventHandler
    fun on(event: InvoiceCancelledEvent) {
        invoiceRepository.findById(event.invoiceId.value).ifPresent { existing ->
            val updated =
                InvoiceEntity(
                    invoiceId = existing.invoiceId,
                    customerId = existing.customerId,
                    bookingId = existing.bookingId,
                    productVariantId = existing.productVariantId,
                    amount = existing.amount,
                    dueDate = existing.dueDate,
                    status = InvoiceStatus.CANCELLED,
                    isInstallment = existing.isInstallment,
                    installmentNumber = existing.installmentNumber,
                    paidAt = existing.paidAt,
                )
            invoiceRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllInvoicesQuery::class.java,
                { true },
                InvoiceUpdated(event.invoiceId.value.toString()),
            )
        }
    }

    @QueryHandler
    fun handle(query: FindAllInvoicesQuery): List<InvoiceView> = findAll()

    @QueryHandler
    fun handle(query: FindInvoiceByIdQuery): InvoiceView? = findById(query.invoiceId)

    fun findAll(): List<InvoiceView> = invoiceRepository.findAll().map { it.toInvoiceView() }

    fun findByStatus(status: InvoiceStatus): List<InvoiceView> = invoiceRepository.findByStatus(status).map { it.toInvoiceView() }

    fun findById(invoiceId: InvoiceId): InvoiceView? = invoiceRepository.findById(invoiceId.value).map { it.toInvoiceView() }.orElse(null)

    fun findByCustomerId(customerId: String): List<InvoiceView> =
        invoiceRepository.findByCustomerId(UUID.fromString(customerId)).map { it.toInvoiceView() }

    private fun InvoiceEntity.toInvoiceView(): InvoiceView {
        val customer = customerProjection.findById(CustomerId.from(this.customerId.toString()))
        val customerName =
            if (customer != null) {
                "${customer.firstName} ${customer.lastName}"
            } else {
                "Unknown"
            }

        return InvoiceView(
            invoiceId = this.invoiceId.toString(),
            customerId = this.customerId.toString(),
            customerName = customerName,
            bookingId = this.bookingId.toString(),
            amount = this.amount,
            dueDate = this.dueDate,
            status = this.status,
            isInstallment = this.isInstallment,
            installmentNumber = this.installmentNumber,
            paidAt = this.paidAt,
        )
    }
}

data class InvoiceView(
    val invoiceId: String,
    val customerId: String,
    val customerName: String,
    val bookingId: String,
    val amount: BigDecimal,
    val dueDate: LocalDate,
    val status: InvoiceStatus,
    val isInstallment: Boolean,
    val installmentNumber: Int?,
    val paidAt: Instant?,
)
