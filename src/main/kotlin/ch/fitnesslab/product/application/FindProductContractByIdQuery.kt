package ch.fitnesslab.product.application

import ch.fitnesslab.domain.value.ProductContractId

data class FindProductContractByIdQuery(
    val contractId: ProductContractId,
)
