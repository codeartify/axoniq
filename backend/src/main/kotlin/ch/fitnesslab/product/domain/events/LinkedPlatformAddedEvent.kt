package ch.fitnesslab.product.domain.events

import ch.fitnesslab.domain.value.ProductId
import ch.fitnesslab.product.domain.LinkedPlatformSync
import org.axonframework.serialization.Revision

@Revision("2.0")
data class LinkedPlatformAddedEvent(
    val productId: ProductId,
    val linkedPlatforms: List<LinkedPlatformSync>? = null,
)
