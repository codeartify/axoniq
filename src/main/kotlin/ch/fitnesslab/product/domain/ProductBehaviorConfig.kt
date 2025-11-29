package ch.fitnesslab.product.domain

data class ProductBehaviorConfig(
    val isTimeBased: Boolean,
    val isSessionBased: Boolean,
    val canBePaused: Boolean,
    val autoRenew: Boolean,
    val renewalLeadTimeDays: Int?,
    val contributesToMembershipStatus: Boolean,
    val maxActivePerCustomer: Int?,
    val exclusivityGroup: String?,
)
