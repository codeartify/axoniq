package com.codeartify.axoniq.domain.commands

import com.codeartify.axoniq.domain.WorkoutId

data class StartWorkoutCommand(val id: WorkoutId)
