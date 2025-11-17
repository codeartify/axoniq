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
    val behavior: ProductBehaviorConfig
)

enum class ProductAudience {
    INTERNAL,
    EXTERNAL,
    BOTH
}

data class ProductBehaviorConfig(
    val isTimeBased: Boolean,
    val isSessionBased: Boolean,
    val canBePaused: Boolean,
    val autoRenew: Boolean,
    val renewalLeadTimeDays: Int?,
    val contributesToMembershipStatus: Boolean,
    val maxActivePerCustomer: Int?,
    val exclusivityGroup: String?
)
