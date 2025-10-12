package com.codeartify.axoniq.domain.exception

data class RepetitionInvalidException(override val message: String): RuntimeException(message)
