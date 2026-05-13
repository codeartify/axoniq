package ch.fitnesslab.product.domain.events

import ch.fitnesslab.domain.value.ProductId
import ch.fitnesslab.product.domain.LinkedPlatformSync
import org.axonframework.eventsourcing.annotation.EventTag

data class LinkedPlatformAddedEvent(
    @field:EventTag(key = "Product")
    val productId: ProductId,
    val linkedPlatforms: List<LinkedPlatformSync>? = null,
)
