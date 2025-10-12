package com.codeartify.axoniq.domain

import com.codeartify.axoniq.domain.values.Repetitions
import com.codeartify.axoniq.domain.values.SetId
import com.codeartify.axoniq.domain.values.Weight

data class Set(
    val id: SetId,
    val repetitions: Repetitions,
    val weight: Weight
)
