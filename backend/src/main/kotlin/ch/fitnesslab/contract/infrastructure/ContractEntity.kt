package ch.fitnesslab.contract.infrastructure

import ch.fitnesslab.contract.application.PauseHistoryEntry
import ch.fitnesslab.domain.ContractStatus
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "contracts")
class ContractEntity(
    @Id
    val contractId: UUID,
    val customerId: UUID,
    val productVariantId: UUID,
    val bookingId: UUID,
    val status: ContractStatus,
    val validityStart: LocalDate?,
    val validityEnd: LocalDate?,
    val sessionsTotal: Int?,
    val sessionsUsed: Int,
    @JdbcTypeCode(SqlTypes.JSON)
    val pauseHistory: List<PauseHistoryEntry> = emptyList(),
)
