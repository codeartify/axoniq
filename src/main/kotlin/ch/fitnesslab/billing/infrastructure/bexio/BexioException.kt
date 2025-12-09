package ch.fitnesslab.billing.infrastructure.bexio

class BexioException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
