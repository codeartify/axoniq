package com.codeartify.axoniq.domain.commands

import com.codeartify.axoniq.domain.values.WorkoutId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class FinishWorkoutCommand(
    @TargetAggregateIdentifier
    val id: WorkoutId
)
