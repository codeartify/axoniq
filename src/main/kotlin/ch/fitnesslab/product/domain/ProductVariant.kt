package ch.fitnesslab.product.domain

import ch.fitnesslab.common.types.ProductVariantId
import java.math.BigDecimal

data class ProductVariant(
    val id: ProductVariantId,
    val code: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean,
    val price: BigDecimal,
    val behavior: ProductBehaviorConfig,
)
