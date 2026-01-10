package ch.fitnesslab.product.domain

import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductContractId
import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.product.domain.ProductContractStatus.*
import ch.fitnesslab.product.domain.commands.CreateProductContractCommand
import ch.fitnesslab.product.domain.commands.PauseProductContractCommand
import ch.fitnesslab.product.domain.commands.ResumeProductContractCommand
import ch.fitnesslab.product.domain.events.ProductContractPausedEvent
import ch.fitnesslab.product.domain.events.ProductContractResumedEvent
import ch.fitnesslab.product.domain.events.ProductContractSignedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.time.temporal.ChronoUnit.DAYS

@Aggregate
class ProductContract() {
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
        AggregateLifecycle.apply(
            ProductContractSignedEvent(
                contractId = command.contractId,
                customerId = command.customerId,
                productVariantId = command.productVariantId,
                bookingId = command.bookingId,
                validity = command.validity,
                sessionsTotal = command.sessionsTotal,
            ),
        )
    }

    @CommandHandler
    fun handle(command: PauseProductContractCommand) {
        require(status == ACTIVE) {
            "Contract must be ACTIVE to be paused"
        }

        val pauseRange = command.pauseRange
        val pauseDays = DAYS.between(pauseRange.start, pauseRange.end)
        require(isValidPauseDuration(pauseDays)) {
            "Pause duration must be between 3 and 8 weeks (21-56 days)"
        }

        AggregateLifecycle.apply(
            ProductContractPausedEvent(
                contractId = command.contractId,
                pauseRange = pauseRange,
                reason = command.reason,
            ),
        )
    }

    private fun isValidPauseDuration(pauseDays: Long): Boolean = pauseDays in 21..56

    @CommandHandler
    fun handle(command: ResumeProductContractCommand) {
        require(status == PAUSED) {
            "Contract must be PAUSED to be resumed"
        }

        val extendedValidity = extendValidityByPause()

        AggregateLifecycle.apply(
            ProductContractResumedEvent(
                contractId = command.contractId,
                extendedValidity = extendedValidity,
            ),
        )
    }

    private fun extendValidityByPause(): DateRange = validity!!.extendBy(lastPauseDurationInDays())

    private fun lastPauseDurationInDays(): Long {
        val lastPause = pauseHistory.last()
        val pauseRange = lastPause.pauseRange
        return DAYS.between(pauseRange.start, pauseRange.end)
    }

    @EventSourcingHandler
    fun on(event: ProductContractSignedEvent) {
        this.contractId = event.contractId
        this.customerId = event.customerId
        this.productVariantId = event.productVariantId
        this.bookingId = event.bookingId
        this.status = ACTIVE
        this.validity = event.validity
        this.sessionsTotal = event.sessionsTotal
        this.sessionsUsed = 0
    }

    @EventSourcingHandler
    fun on(event: ProductContractPausedEvent) {
        this.status = PAUSED
        this.pauseHistory.add(PauseEntry(event.pauseRange, event.reason))
    }

    @EventSourcingHandler
    fun on(event: ProductContractResumedEvent) {
        this.status = ACTIVE
        this.validity = event.extendedValidity
    }
}
