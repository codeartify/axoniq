package ch.fitnesslab.product.domain

import ch.fitnesslab.common.types.BookingId
import ch.fitnesslab.common.types.CustomerId
import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.commands.CreateProductContractCommand
import ch.fitnesslab.product.domain.commands.PauseProductContractCommand
import ch.fitnesslab.product.domain.commands.PauseReason
import ch.fitnesslab.product.domain.commands.ResumeProductContractCommand
import ch.fitnesslab.product.domain.events.ProductContractCreatedEvent
import ch.fitnesslab.product.domain.events.ProductContractPausedEvent
import ch.fitnesslab.product.domain.events.ProductContractResumedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.time.temporal.ChronoUnit

@Aggregate
class ProductContractAggregate() {

    @AggregateIdentifier
    private lateinit var contractId: ProductContractId
    private lateinit var customerId: CustomerId
    private lateinit var productVariantId: ProductVariantId
    private lateinit var bookingId: BookingId
    private lateinit var status: ProductContractStatus
    private var validity: DateRange? = null
    private var sessionsTotal: Int? = null
    private var sessionsUsed: Int = 0
    private val pauseHistory: MutableList<PauseEntry> = mutableListOf()

    @CommandHandler
    constructor(command: CreateProductContractCommand) : this() {
        // For memberships, activate immediately
        // For session-based products, wait for payment
        val initialStatus = if (command.sessionsTotal == null) {
            ProductContractStatus.ACTIVE
        } else {
            ProductContractStatus.PENDING_ACTIVATION
        }

        AggregateLifecycle.apply(
            ProductContractCreatedEvent(
                contractId = command.contractId,
                customerId = command.customerId,
                productVariantId = command.productVariantId,
                bookingId = command.bookingId,
                status = initialStatus,
                validity = command.validity,
                sessionsTotal = command.sessionsTotal
            )
        )
    }

    @CommandHandler
    fun handle(command: PauseProductContractCommand) {
        require(status == ProductContractStatus.ACTIVE) {
            "Contract must be ACTIVE to be paused"
        }

        val pauseDays = ChronoUnit.DAYS.between(command.pauseRange.start, command.pauseRange.end)
        require(pauseDays in 21..56) {
            "Pause duration must be between 3 and 8 weeks (21-56 days)"
        }

        AggregateLifecycle.apply(
            ProductContractPausedEvent(
                contractId = command.contractId,
                pauseRange = command.pauseRange,
                reason = command.reason
            )
        )
    }

    @CommandHandler
    fun handle(command: ResumeProductContractCommand) {
        require(status == ProductContractStatus.PAUSED) {
            "Contract must be PAUSED to be resumed"
        }

        val lastPause = pauseHistory.last()
        val pauseDays = ChronoUnit.DAYS.between(lastPause.pauseRange.start, lastPause.pauseRange.end)
        val extendedValidity = validity!!.extendBy(pauseDays)

        AggregateLifecycle.apply(
            ProductContractResumedEvent(
                contractId = command.contractId,
                extendedValidity = extendedValidity
            )
        )
    }

    @EventSourcingHandler
    fun on(event: ProductContractCreatedEvent) {
        this.contractId = event.contractId
        this.customerId = event.customerId
        this.productVariantId = event.productVariantId
        this.bookingId = event.bookingId
        this.status = event.status
        this.validity = event.validity
        this.sessionsTotal = event.sessionsTotal
        this.sessionsUsed = 0
    }

    @EventSourcingHandler
    fun on(event: ProductContractPausedEvent) {
        this.status = ProductContractStatus.PAUSED
        this.pauseHistory.add(PauseEntry(event.pauseRange, event.reason))
    }

    @EventSourcingHandler
    fun on(event: ProductContractResumedEvent) {
        this.status = ProductContractStatus.ACTIVE
        this.validity = event.extendedValidity
    }
}

data class PauseEntry(
    val pauseRange: DateRange,
    val reason: PauseReason
)
