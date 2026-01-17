package ch.fitnesslab.product.application

import ch.fitnesslab.domain.value.ProductId

data class FindAllProductsQuery(
    val timestamp: Long = System.currentTimeMillis(),
)

data class FindProductByIdQuery(
    val productId: ProductId,
)

data class ProductUpdatedUpdate(
    val productId: String,
)
