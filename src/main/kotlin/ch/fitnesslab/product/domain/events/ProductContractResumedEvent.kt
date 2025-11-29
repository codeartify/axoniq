package ch.fitnesslab.product.domain.events

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ProductContractResumedEvent(
    val contractId: ProductContractId,
    val extendedValidity: DateRange,
)
