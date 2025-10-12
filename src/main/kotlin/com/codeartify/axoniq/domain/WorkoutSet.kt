package com.codeartify.axoniq.domain

import com.codeartify.axoniq.domain.values.Repetitions
import com.codeartify.axoniq.domain.values.Weight

data class WorkoutSet(
    val id: WorkoutSetId,
    val repetitions: Repetitions,
    val weight: Weight
)
