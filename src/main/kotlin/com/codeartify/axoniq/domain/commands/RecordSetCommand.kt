package com.codeartify.axoniq.domain.commands

import com.codeartify.axoniq.domain.values.*
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class RecordSetCommand(
    @TargetAggregateIdentifier val workoutId: WorkoutId,
    val setId: SetId,
    val exerciseName: ExerciseName,
    val repetitions: Repetitions,
    val weight: Weight
)
