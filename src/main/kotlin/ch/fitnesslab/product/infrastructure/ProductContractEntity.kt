package ch.fitnesslab.product.infrastructure

import ch.fitnesslab.product.domain.ProductContractStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "product_contracts")
class ProductContractEntity(
    @Id
    @Column(name = "contract_id")
    val contractId: UUID,

    @Column(name = "customer_id", nullable = false)
    val customerId: UUID,

    @Column(name = "product_variant_id", nullable = false)
    val productVariantId: UUID,

    @Column(name = "booking_id", nullable = false)
    val bookingId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ProductContractStatus,

    @Column(name = "validity_start", nullable = true)
    val validityStart: LocalDate?,

    @Column(name = "validity_end", nullable = true)
    val validityEnd: LocalDate?,

    @Column(name = "sessions_total", nullable = true)
    val sessionsTotal: Int?,

    @Column(name = "sessions_used", nullable = false)
    val sessionsUsed: Int
)
