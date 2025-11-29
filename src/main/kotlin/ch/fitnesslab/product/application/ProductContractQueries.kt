package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.ProductContractId

data class FindAllProductContractsQuery(
    val timestamp: Long = System.currentTimeMillis(),
)

data class FindProductContractByIdQuery(
    val contractId: ProductContractId,
)

data class ProductContractUpdatedUpdate(
    val contractId: String,
)
