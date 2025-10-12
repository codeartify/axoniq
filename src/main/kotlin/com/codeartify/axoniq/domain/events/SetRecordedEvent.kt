package com.codeartify.axoniq.domain.events

import com.codeartify.axoniq.domain.values.*

data class SetRecordedEvent(
    val setId: SetId,
    val workoutId: WorkoutId,
    val exerciseName: ExerciseName,
    val repetitions: Repetitions,
    val weight: Weight
)
