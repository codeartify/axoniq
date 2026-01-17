package ch.fitnesslab.contract.application

import ch.fitnesslab.domain.ContractStatus
import ch.fitnesslab.domain.value.DateRange

data class ContractView(
    val contractId: String,
    val customerId: String,
    val productVariantId: String,
    val bookingId: String,
    val status: ContractStatus,
    val validity: DateRange?,
    val sessionsTotal: Int?,
    val sessionsUsed: Int,
    val pauseHistory: List<PauseHistoryEntry>,
)
