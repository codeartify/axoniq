package ch.fitnesslab.product.infrastructure

import ch.fitnesslab.product.domain.ProductAudience
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @Column(name = "product_id")
    val productId: UUID,
    @Column(nullable = false, unique = true)
    val code: String,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val productType: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val audience: ProductAudience,
    @Column(nullable = false)
    val requiresMembership: Boolean,
    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,
    // ProductBehaviorConfig fields
    @Column(nullable = false)
    val isTimeBased: Boolean,
    @Column(nullable = false)
    val isSessionBased: Boolean,
    @Column(nullable = false)
    val canBePaused: Boolean,
    @Column(nullable = false)
    val autoRenew: Boolean,
    @Column(nullable = true)
    val renewalLeadTimeDays: Int?,
    @Column(nullable = false)
    val contributesToMembershipStatus: Boolean,
    @Column(nullable = true)
    val maxActivePerCustomer: Int?,
    @Column(nullable = true)
    val exclusivityGroup: String?,
)
