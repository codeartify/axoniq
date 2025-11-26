package ch.fitnesslab.product.domain.commands

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal

data class CreateProductCommand(
    @TargetAggregateIdentifier
    val productId: ProductVariantId,
    val code: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean,
    val price: BigDecimal,
    val behavior: ProductBehaviorConfig
)
