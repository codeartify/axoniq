package ch.fitnesslab.product.infrastructure

import ch.fitnesslab.product.domain.ProductAudience
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "products")
class ProductVariantEntity(
    @Id
    val productId: UUID,
    val code: String,
    val name: String,
    val productType: String,
    val audience: ProductAudience,
    val requiresMembership: Boolean = false,
    val price: BigDecimal,
    val durationInMonths: Int? = null,
    val renewalLeadTimeDays: Int? = null,
    val numberOfSessions: Int? = null,
    val canBePaused: Boolean = false,
    val maxActivePerCustomer: Int? = null,
)
