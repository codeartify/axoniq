package com.codeartify.axoniq.domain.events

import com.codeartify.axoniq.domain.WorkoutSetId
import com.codeartify.axoniq.domain.values.Repetitions
import com.codeartify.axoniq.domain.values.Weight
import com.codeartify.axoniq.domain.values.WorkoutId

data class SetRecordedEvent(
    val setId: WorkoutSetId,
    val workoutId: WorkoutId,
    val repetitions: Repetitions,
    val weight: Weight
)
