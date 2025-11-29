package ch.fitnesslab.product.domain.events

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.product.domain.commands.PauseReason
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ProductContractPausedEvent(
    val contractId: ProductContractId,
    val pauseRange: DateRange,
    val reason: PauseReason,
)
