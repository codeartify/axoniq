package ch.fitnesslab.contract.infrastructure

import ch.fitnesslab.domain.ContractStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ContractRepository : JpaRepository<ContractEntity, UUID> {
    fun findByCustomerId(customerId: UUID): List<ContractEntity>

    fun findByStatus(status: ContractStatus): List<ContractEntity>
}
