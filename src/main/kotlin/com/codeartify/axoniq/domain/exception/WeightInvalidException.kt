package com.codeartify.axoniq.domain.exception

data class WeightInvalidException(override val message: String): RuntimeException(message)
