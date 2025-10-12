package com.codeartify.axoniq.domain.exception

data class FinishingWorkoutFailedException(override val message: String) : RuntimeException(message)
