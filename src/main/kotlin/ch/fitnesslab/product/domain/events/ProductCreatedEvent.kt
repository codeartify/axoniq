package ch.fitnesslab.product.domain.events

import ch.fitnesslab.common.types.ProductVariantId
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductBehaviorConfig
import org.axonframework.serialization.Revision
import java.math.BigDecimal

@Revision("1.0")
data class ProductCreatedEvent(
    val productId: ProductVariantId,
    val code: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean,
    val price: BigDecimal,
    val behavior: ProductBehaviorConfig
)
