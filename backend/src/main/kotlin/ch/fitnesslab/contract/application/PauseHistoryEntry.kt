package ch.fitnesslab.contract.application

import ch.fitnesslab.domain.PauseReason
import ch.fitnesslab.domain.value.DateRange

data class PauseHistoryEntry(
    val pauseRange: DateRange,
    val reason: PauseReason,
)
