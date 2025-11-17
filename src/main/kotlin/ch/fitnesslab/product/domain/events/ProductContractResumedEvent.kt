package ch.fitnesslab.product.domain.events

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId

data class ProductContractResumedEvent(
    val contractId: ProductContractId,
    val extendedValidity: DateRange
)
