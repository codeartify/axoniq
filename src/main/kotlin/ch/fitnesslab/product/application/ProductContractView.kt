package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.product.domain.ProductContractStatus

data class ProductContractView(
    val contractId: String,
    val customerId: String,
    val productVariantId: String,
    val bookingId: String,
    val status: ProductContractStatus,
    val validity: DateRange?,
    val sessionsTotal: Int?,
    val sessionsUsed: Int,
    val pauseHistory: List<PauseHistoryEntry>
)
