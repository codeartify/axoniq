package com.codeartify.axoniq.domain.commands

import com.codeartify.axoniq.domain.values.Repetitions
import com.codeartify.axoniq.domain.values.Weight
import com.codeartify.axoniq.domain.values.WorkoutId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class RecordSetCommand(
    @TargetAggregateIdentifier val workoutId: WorkoutId,
    val repetitions: Repetitions,
    val weight: Weight
)
