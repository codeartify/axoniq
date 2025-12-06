package ch.fitnesslab.product.domain

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.product.domain.commands.PauseReason

data class PauseEntry(
    val pauseRange: DateRange,
    val reason: PauseReason,
)
