package ch.fitnesslab.product.domain

data class PricingDuration(
    val interval: BillingInterval,
    val count: Int
)
