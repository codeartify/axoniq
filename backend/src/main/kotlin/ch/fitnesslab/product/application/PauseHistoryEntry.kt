package ch.fitnesslab.product.application

import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.product.domain.commands.PauseReason

data class PauseHistoryEntry(
    val pauseRange: DateRange,
    val reason: PauseReason,
)
