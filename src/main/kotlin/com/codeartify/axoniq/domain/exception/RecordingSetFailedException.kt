package com.codeartify.axoniq.domain.exception

data class RecordingSetFailedException(override val message: String): RuntimeException(message)
