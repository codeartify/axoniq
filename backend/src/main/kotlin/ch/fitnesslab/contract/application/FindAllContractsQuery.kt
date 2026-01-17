package ch.fitnesslab.contract.application

data class FindAllContractsQuery(
    val timestamp: Long = System.currentTimeMillis(),
)
