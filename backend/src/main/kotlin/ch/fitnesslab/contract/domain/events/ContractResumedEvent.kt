package ch.fitnesslab.contract.domain.events

import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.DateRange
import org.axonframework.serialization.Revision

@Revision("1.0")
data class ContractResumedEvent(
    val contractId: ContractId,
    val extendedValidity: DateRange,
)
