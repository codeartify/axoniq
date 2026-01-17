package ch.fitnesslab.billing.domain

import ch.fitnesslab.billing.domain.commands.CancelInvoiceCommand
import ch.fitnesslab.billing.domain.commands.CreateInvoiceCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoiceOverdueCommand
import ch.fitnesslab.billing.domain.commands.MarkInvoicePaidCommand
import ch.fitnesslab.billing.domain.events.InvoiceCancelledEvent
import ch.fitnesslab.billing.domain.events.InvoiceCreatedEvent
import ch.fitnesslab.billing.domain.events.InvoiceMarkedOverdueEvent
import ch.fitnesslab.billing.domain.events.InvoicePaidEvent
import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.InvoiceId
import ch.fitnesslab.domain.value.ProductId
import ch.fitnesslab.membership.domain.DueDate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.math.BigDecimal
import java.time.Instant

@Aggregate
class Invoice() {
    @AggregateIdentifier
    private lateinit var invoiceId: InvoiceId
    private lateinit var bookingId: BookingId
    private lateinit var customerId: CustomerId
    private var productId: ProductId? = null
    private lateinit var amount: BigDecimal
    private lateinit var dueDate: DueDate
    private lateinit var status: InvoiceStatus
    private var isInstallment: Boolean = false
    private var installmentNumber: Int? = null
    private var paidAt: Instant? = null

    @CommandHandler
    constructor(command: CreateInvoiceCommand) : this() {
        AggregateLifecycle.apply(
            InvoiceCreatedEvent(
                invoiceId = command.invoiceId,
                bookingId = command.bookingId,
                customerId = command.customerId,
                productId = command.productId,
                amount = command.amount,
                dueDate = command.dueDate,
                status = InvoiceStatus.OPEN,
                isInstallment = command.isInstallment,
                installmentNumber = command.installmentNumber,
            ),
        )
    }

    @CommandHandler
    fun handle(command: MarkInvoicePaidCommand) {
        require(status == InvoiceStatus.OPEN || status == InvoiceStatus.OVERDUE) {
            "Invoice must be OPEN or OVERDUE to be marked as PAID"
        }

        AggregateLifecycle.apply(
            InvoicePaidEvent(
                invoiceId = command.invoiceId,
                paidAt = command.paidAt,
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: InvoiceCreatedEvent) {
        this.invoiceId = event.invoiceId
        this.bookingId = event.bookingId
        this.customerId = event.customerId
        this.productId = event.productId
        this.amount = event.amount
        this.dueDate = event.dueDate
        this.status = event.status
        this.isInstallment = event.isInstallment
        this.installmentNumber = event.installmentNumber
    }

    @CommandHandler
    fun handle(command: MarkInvoiceOverdueCommand) {
        require(status == InvoiceStatus.OPEN) {
            "Invoice must be OPEN to be marked as OVERDUE"
        }

        AggregateLifecycle.apply(
            InvoiceMarkedOverdueEvent(
                invoiceId = command.invoiceId,
            ),
        )
    }

    @CommandHandler
    fun handle(command: CancelInvoiceCommand) {
        require(status != InvoiceStatus.PAID) {
            "Cannot cancel a PAID invoice"
        }

        AggregateLifecycle.apply(
            InvoiceCancelledEvent(
                invoiceId = command.invoiceId,
                reason = command.reason,
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: InvoicePaidEvent) {
        this.status = InvoiceStatus.PAID
        this.paidAt = event.paidAt
    }

    @EventSourcingHandler
    fun on(event: InvoiceMarkedOverdueEvent) {
        this.status = InvoiceStatus.OVERDUE
    }

    @EventSourcingHandler
    fun on(event: InvoiceCancelledEvent) {
        this.status = InvoiceStatus.CANCELLED
    }
}
