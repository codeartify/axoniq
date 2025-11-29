package ch.fitnesslab.product.infrastructure

import ch.fitnesslab.product.domain.ProductContractStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductContractRepository : JpaRepository<ProductContractEntity, UUID> {
    fun findByCustomerId(customerId: UUID): List<ProductContractEntity>

    fun findByStatus(status: ProductContractStatus): List<ProductContractEntity>
}
