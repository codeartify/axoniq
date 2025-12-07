package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.LinkedPlatformSync
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import ch.fitnesslab.product.domain.PricingVariantConfig
import ch.fitnesslab.product.domain.ProductVisibility
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class UpdateProductCommand(
    @TargetAggregateIdentifier
    val productId: ProductVariantId,
    val slug: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean,
    val pricingVariant: PricingVariantConfig,
    val behavior: ProductBehaviorConfig,
    val description: String? = null,
    val termsAndConditions: String? = null,
    val visibility: ProductVisibility = ProductVisibility.PUBLIC,
    val buyable: Boolean = true,
    val buyerCanCancel: Boolean = true,
    val perks: List<String>? = null,
    val linkedPlatforms: List<LinkedPlatformSync>? = null,
)
