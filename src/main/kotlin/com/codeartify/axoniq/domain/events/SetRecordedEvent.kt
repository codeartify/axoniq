package com.codeartify.axoniq.domain.events

import com.codeartify.axoniq.domain.WorkoutId

data class SetRecordedEvent(val workoutId: WorkoutId)
