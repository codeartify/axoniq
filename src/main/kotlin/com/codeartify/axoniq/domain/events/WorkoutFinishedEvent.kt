package com.codeartify.axoniq.domain.events

import com.codeartify.axoniq.domain.values.WorkoutId


data class WorkoutFinishedEvent(val id: WorkoutId)
