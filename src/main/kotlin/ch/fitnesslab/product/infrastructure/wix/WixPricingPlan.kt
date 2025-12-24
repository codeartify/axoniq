package ch.fitnesslab.product.infrastructure.wix.v3

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.OffsetDateTime

// =====================
// Create Plan Response
// =====================

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixCreatePlanResponse(
    val plan: WixPlan? = null,
)

// Alias for GET plan by ID response (same structure as create)
typealias WixPlanResponse = WixCreatePlanResponse

// =====================
// Top-level response
// =====================

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixQueryPlansResponse(
    val pagingMetadata: WixPagingMetadata? = null,
    val plans: List<WixPlan> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPagingMetadata(
    val count: Int? = null,
    val offset: Int? = null,
    val cursors: WixPagingCursors? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPagingCursors(
    val next: String? = null,
    val prev: String? = null,
)

// =====================
// Plan
// =====================

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPlan(
    val id: String? = null,
    val revision: String? = null,
    val createdDate: OffsetDateTime? = null,
    val updatedDate: OffsetDateTime? = null,
    val name: String? = null,
    val slug: String? = null,
    val description: String? = null,
    val maxPurchasesPerBuyer: Int? = null,
    // V3: array of PricingVariant (usually max 1)
    val pricingVariants: List<WixPricingVariantV3> = emptyList(),
    // Perks: array of Perk objects
    val perks: List<WixPerk> = emptyList(),
    // Plan visibility / lifecycle
    val visibility: String? = null, // e.g. "PUBLIC"
    val buyable: Boolean? = null,
    val status: String? = null, // e.g. "ACTIVE"
    val buyerCanCancel: Boolean? = null,
    // Extra fields shown in your example
    val archived: Boolean? = null,
    val primary: Boolean? = null,
    val currency: String? = null, // e.g. "EUR"
)

// =====================
// Pricing variant
// =====================

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPricingVariantV3(
    val id: String? = null,
    val name: String? = null,
    // Number of free trial days before first charge
    val freeTrialDays: Int? = null,
    // Unknown structure in sample -> keep as Map to be safe
    val fees: List<Map<String, Any?>> = emptyList(),
    val billingTerms: WixBillingTerms? = null,
    // List of pricing strategies (flat rate etc.)
    val pricingStrategies: List<WixPricingStrategy> = emptyList(),
)

// =====================
// Billing terms & cycle
// =====================

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixBillingTerms(
    val billingCycle: WixBillingCycle? = null,
    val startType: String? = null, // e.g. "ON_PURCHASE"
    val endType: String? = null, // e.g. "CYCLES_COMPLETED", "UNTIL_CANCELLED"
    val cyclesCompletedDetails: WixCyclesCompletedDetails? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixBillingCycle(
    val period: String? = null, // "DAY", "WEEK", "MONTH", "YEAR"
    val count: Int? = null, // how many periods per cycle (docs specify integer)
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixCyclesCompletedDetails(
    val billingCycleCount: Int? = null,
)

// =====================
// Pricing strategy
// =====================

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPricingStrategy(
    val flatRate: WixFlatRate? = null,
    // Future-proof: other strategy types can be added here later
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixFlatRate(
    val amount: String? = null, // "5.99" as in example (string, not number)
)

// =====================
// Perks
// =====================

@JsonIgnoreProperties(ignoreUnknown = true)
data class WixPerk(
    val id: String? = null,
    val description: String? = null,
)
