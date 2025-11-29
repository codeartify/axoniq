package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.product.domain.commands.PauseReason

data class PauseHistoryEntry(
    val pauseRange: DateRange,
    val reason: PauseReason
)
