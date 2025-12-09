package ch.fitnesslab.product.domain

import java.math.BigDecimal

data class PricingVariantConfig(
    val pricingModel: PricingModel,
    val flatRate: BigDecimal,
    val billingCycle: PricingDuration?,
    val duration: PricingDuration?,
    val freeTrial: PricingDuration?,
)
