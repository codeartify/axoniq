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
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.messaging.eventhandling.gateway.EventAppender
import java.math.BigDecimal
import java.time.Instant

@EventSourced(idType = InvoiceId::class, tagKey = "Invoice")
class Invoice {
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

    @EntityCreator
    constructor()

    companion object {
        @JvmStatic
        @CommandHandler
        fun handle(
            command: CreateInvoiceCommand,
            eventAppender: EventAppender,
        ) {
            eventAppender.append(
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
    }

    @CommandHandler
    fun handle(
        command: MarkInvoicePaidCommand,
        eventAppender: EventAppender,
    ) {
        require(status == InvoiceStatus.OPEN || status == InvoiceStatus.OVERDUE) {
            "Invoice must be OPEN or OVERDUE to be marked as PAID"
        }

        eventAppender.append(
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
    fun handle(
        command: MarkInvoiceOverdueCommand,
        eventAppender: EventAppender,
    ) {
        require(status == InvoiceStatus.OPEN) {
            "Invoice must be OPEN to be marked as OVERDUE"
        }

        eventAppender.append(
            InvoiceMarkedOverdueEvent(
                invoiceId = command.invoiceId,
            ),
        )
    }

    @CommandHandler
    fun handle(
        command: CancelInvoiceCommand,
        eventAppender: EventAppender,
    ) {
        require(status != InvoiceStatus.PAID) {
            "Cannot cancel a PAID invoice"
        }

        eventAppender.append(
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
