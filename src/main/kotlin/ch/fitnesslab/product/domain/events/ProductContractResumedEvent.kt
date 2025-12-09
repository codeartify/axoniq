package ch.fitnesslab.product.domain.events

import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductContractId
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ProductContractResumedEvent(
    val contractId: ProductContractId,
    val extendedValidity: DateRange,
)
