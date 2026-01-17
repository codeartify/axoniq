package ch.fitnesslab.plugins.bexio.invoice

class BexioException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
