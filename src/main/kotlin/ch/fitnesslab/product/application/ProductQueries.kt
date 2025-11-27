package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.ProductVariantId

data class FindAllProductsQuery(val timestamp: Long = System.currentTimeMillis())

data class FindProductByIdQuery(val productId: ProductVariantId)

data class ProductUpdatedUpdate(val productId: String)
