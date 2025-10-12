package com.codeartify.axoniq.domain.commands

import com.codeartify.axoniq.domain.WorkoutSetId
import com.codeartify.axoniq.domain.values.Repetitions
import com.codeartify.axoniq.domain.values.Weight
import com.codeartify.axoniq.domain.values.WorkoutId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class RecordSetCommand(
    @TargetAggregateIdentifier val workoutId: WorkoutId,
    val setId: WorkoutSetId,
    val repetitions: Repetitions,
    val weight: Weight
)
