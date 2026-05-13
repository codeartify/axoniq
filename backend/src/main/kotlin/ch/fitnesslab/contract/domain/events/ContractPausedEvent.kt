package ch.fitnesslab.contract.domain.events

import ch.fitnesslab.domain.PauseReason
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.DateRange
import org.axonframework.eventsourcing.annotation.EventTag

data class ContractPausedEvent(
    @field:EventTag(key = "Contract")
    val contractId: ContractId,
    val pauseRange: DateRange,
    val reason: PauseReason,
)
