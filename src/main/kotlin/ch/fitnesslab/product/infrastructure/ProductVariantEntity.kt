package ch.fitnesslab.product.infrastructure

import ch.fitnesslab.product.domain.BillingInterval
import ch.fitnesslab.product.domain.PricingModel
import ch.fitnesslab.product.domain.ProductAudience
import ch.fitnesslab.product.domain.ProductVisibility
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "products")
class ProductVariantEntity(
    @Id
    val productId: UUID,
    val slug: String,
    val name: String,
    val productType: String,
    @Enumerated(EnumType.STRING)
    val audience: ProductAudience,
    val requiresMembership: Boolean = false,

    // PricingVariantConfig fields (flattened)
    @Enumerated(EnumType.STRING)
    val pricingModel: PricingModel,
    val flatRate: BigDecimal,
    @Enumerated(EnumType.STRING)
    val billingCycleInterval: BillingInterval? = null,
    val billingCycleCount: Int? = null,
    @Enumerated(EnumType.STRING)
    val durationInterval: BillingInterval? = null,
    val durationCount: Int? = null,
    @Enumerated(EnumType.STRING)
    val freeTrialInterval: BillingInterval? = null,
    val freeTrialCount: Int? = null,

    // ProductBehaviorConfig fields (flattened)
    val canBePaused: Boolean = false,
    val renewalLeadTimeDays: Int? = null,
    val maxActivePerCustomer: Int? = null,
    val maxPurchasesPerBuyer: Int? = null,
    val numberOfSessions: Int? = null,

    // New fields
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    @Column(columnDefinition = "TEXT")
    val termsAndConditions: String? = null,
    @Enumerated(EnumType.STRING)
    val visibility: ProductVisibility = ProductVisibility.PUBLIC,
    val buyable: Boolean = true,
    val buyerCanCancel: Boolean = true,
    @ElementCollection
    @CollectionTable(name = "product_perks", joinColumns = [JoinColumn(name = "product_id")])
    @Column(name = "perk")
    val perks: List<String>? = null,
)
