package ch.fitnesslab.product.infrastructure.wix

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPricingPlansResponse(
    val plans: List<WixPlan>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPlan(
    val id: String,
    val revision: String?,
    val name: String,
    val description: String?,
    val slug: String?,
    val image: WixImage?,
    val termsAndConditions: String?,
    val maxPurchasesPerBuyer: Int?,
    val perks: List<WixPerk>?,
    val visibility: String?, // "PUBLIC", "HIDDEN", "ARCHIVED"
    val buyable: Boolean,
    val buyerCanCancel: Boolean,
    val formId: String?,
    val currency: String?,
    val pricingVariants: List<WixPricingVariant>?,
    val extendedFields: Map<String, Any>?,
    val createdDate: OffsetDateTime?,
    val updatedDate: OffsetDateTime?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPricingVariant(
    val pricingModel: String?, // "SUBSCRIPTION", "SINGLE_PAYMENT_FOR_DURATION", "SINGLE_PAYMENT_UNLIMITED"
    val flatRate: Double?,
    val billingCycle: WixDuration?,
    val duration: WixDuration?,
    val freeTrial: WixDuration?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixDuration(
    val interval: String?, // "DAY", "WEEK", "MONTH", "YEAR"
    val count: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixImage(
    val url: String?,
    val altText: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPerk(
    val title: String,
    val description: String?
)
