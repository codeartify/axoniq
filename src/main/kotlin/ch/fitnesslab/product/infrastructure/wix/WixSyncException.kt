package ch.fitnesslab.product.infrastructure.wix

class WixSyncException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
