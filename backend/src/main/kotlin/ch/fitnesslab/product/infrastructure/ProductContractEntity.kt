package ch.fitnesslab.product.infrastructure

import ch.fitnesslab.product.application.PauseHistoryEntry
import ch.fitnesslab.product.domain.ProductContractStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "product_contracts")
class ProductContractEntity(
    @Id
    val contractId: UUID,
    val customerId: UUID,
    val productVariantId: UUID,
    val bookingId: UUID,
    val status: ProductContractStatus,
    val validityStart: LocalDate?,
    val validityEnd: LocalDate?,
    val sessionsTotal: Int?,
    val sessionsUsed: Int,
    @JdbcTypeCode(SqlTypes.JSON)
    val pauseHistory: List<PauseHistoryEntry> = emptyList(),
)
