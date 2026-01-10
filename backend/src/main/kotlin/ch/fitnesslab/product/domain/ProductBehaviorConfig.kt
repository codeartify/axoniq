package ch.fitnesslab.product.domain

data class ProductBehaviorConfig(
    val canBePaused: Boolean,
    val renewalLeadTimeDays: Int?,
    val maxActivePerCustomer: Int?,
    val maxPurchasesPerBuyer: Int?,
    val numberOfSessions: Int?,
)
