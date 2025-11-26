package ch.fitnesslab.booking.domain

import ch.fitnesslab.common.types.ProductVariantId
import java.math.BigDecimal

data class PurchasedProduct(
    val productVariantId: ProductVariantId,
    val participants: List<Participant>,
    val totalPrice: BigDecimal
)

