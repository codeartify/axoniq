package ch.fitnesslab.billing.infrastructure

import ch.fitnesslab.billing.domain.InvoiceStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface InvoiceRepository : JpaRepository<InvoiceEntity, UUID> {
    fun findByStatus(status: InvoiceStatus): List<InvoiceEntity>

    fun findByCustomerId(customerId: UUID): List<InvoiceEntity>
}
