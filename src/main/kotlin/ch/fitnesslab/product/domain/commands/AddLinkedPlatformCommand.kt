package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.product.domain.LinkedPlatformSync
import ch.fitnesslab.product.domain.PricingVariantConfig
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import ch.fitnesslab.product.domain.ProductVisibility
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class AddLinkedPlatformCommand(
    @TargetAggregateIdentifier
    val productId: ProductVariantId,
    val linkedPlatforms: List<LinkedPlatformSync>? = null,
)
