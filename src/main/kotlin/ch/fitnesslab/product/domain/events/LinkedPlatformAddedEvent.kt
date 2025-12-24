package ch.fitnesslab.product.domain.events

import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.product.domain.LinkedPlatformSync
import ch.fitnesslab.product.domain.PricingVariantConfig
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import ch.fitnesslab.product.domain.ProductVisibility
import org.axonframework.serialization.Revision

@Revision("2.0")
data class LinkedPlatformAddedEvent(
    val productId: ProductVariantId,
    val linkedPlatforms: List<LinkedPlatformSync>? = null,
)
