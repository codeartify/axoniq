package ch.fitnesslab.contract.domain

import ch.fitnesslab.contract.domain.commands.CreateContractCommand
import ch.fitnesslab.contract.domain.commands.PauseContractCommand
import ch.fitnesslab.contract.domain.commands.ResumeContractCommand
import ch.fitnesslab.contract.domain.events.ContractPausedEvent
import ch.fitnesslab.contract.domain.events.ContractResumedEvent
import ch.fitnesslab.contract.domain.events.ContractSignedEvent
import ch.fitnesslab.domain.PauseEntry
import ch.fitnesslab.domain.ContractStatus
import ch.fitnesslab.domain.ContractStatus.*
import ch.fitnesslab.domain.value.BookingId
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.ProductVariantId
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import java.time.temporal.ChronoUnit.DAYS

@Aggregate
class Contract() {
    @AggregateIdentifier
    private lateinit var contractId: ContractId
    private lateinit var customerId: CustomerId
    private lateinit var productVariantId: ProductVariantId
    private lateinit var bookingId: BookingId
    private lateinit var status: ContractStatus
    private var validity: DateRange? = null
    private var sessionsTotal: Int? = null
    private var sessionsUsed: Int = 0
    private val pauseHistory: MutableList<PauseEntry> = mutableListOf()

    @CommandHandler
    constructor(command: CreateContractCommand) : this() {
        AggregateLifecycle.apply(
            ContractSignedEvent(
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
    fun handle(command: PauseContractCommand) {
        require(status == ACTIVE) {
            "Contract must be ACTIVE to be paused"
        }

        val pauseRange = command.pauseRange
        val pauseDays = DAYS.between(pauseRange.start, pauseRange.end)
        require(isValidPauseDuration(pauseDays)) {
            "Pause duration must be between 3 and 8 weeks (21-56 days)"
        }

        AggregateLifecycle.apply(
            ContractPausedEvent(
                contractId = command.contractId,
                pauseRange = pauseRange,
                reason = command.reason,
            ),
        )
    }

    private fun isValidPauseDuration(pauseDays: Long): Boolean = pauseDays in 21..56

    @CommandHandler
    fun handle(command: ResumeContractCommand) {
        require(status == PAUSED) {
            "Contract must be PAUSED to be resumed"
        }

        val extendedValidity = extendValidityByPause()

        AggregateLifecycle.apply(
            ContractResumedEvent(
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
    fun on(event: ContractSignedEvent) {
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
    fun on(event: ContractPausedEvent) {
        this.status = PAUSED
        this.pauseHistory.add(PauseEntry(event.pauseRange, event.reason))
    }

    @EventSourcingHandler
    fun on(event: ContractResumedEvent) {
        this.status = ACTIVE
        this.validity = event.extendedValidity
    }
}
