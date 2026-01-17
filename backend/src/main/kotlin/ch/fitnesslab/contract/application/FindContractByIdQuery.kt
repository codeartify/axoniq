package ch.fitnesslab.contract.application

import ch.fitnesslab.domain.value.ContractId

data class FindContractByIdQuery(
    val contractId: ContractId,
)
