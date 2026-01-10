package ch.fitnesslab.billing.application

data class FindAllInvoicesQuery(
    val timestamp: Long = System.currentTimeMillis(),
)
