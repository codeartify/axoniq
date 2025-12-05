package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.ProductContractId

data class FindProductContractByIdQuery(
    val contractId: ProductContractId,
)
