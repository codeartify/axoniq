package ch.fitnesslab.domain

import ch.fitnesslab.domain.value.DateRange

data class PauseEntry(
    val pauseRange: DateRange,
    val reason: PauseReason,
)
