package com.codeartify.axoniq.domain.events

import com.codeartify.axoniq.domain.WorkoutId

data class WorkoutFinishedEvent(val id: WorkoutId)
