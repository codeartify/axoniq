package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.product.domain.ProductContractStatus
import ch.fitnesslab.product.domain.commands.PauseReason
import ch.fitnesslab.product.domain.events.ProductContractSignedEvent
import ch.fitnesslab.product.domain.events.ProductContractPausedEvent
import ch.fitnesslab.product.domain.events.ProductContractResumedEvent
import ch.fitnesslab.product.infrastructure.ProductContractEntity
import ch.fitnesslab.product.infrastructure.ProductContractRepository
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.*
@ProcessingGroup("product-contracts")
@Component
class ProductContractProjection(
    private val productContractRepository: ProductContractRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {

    @EventHandler
    fun on(event: ProductContractSignedEvent) {
        val entity = ProductContractEntity(
            contractId = event.contractId.value,
            customerId = event.customerId.value,
            productVariantId = event.productVariantId.value,
            bookingId = event.bookingId.value,
            status = event.status,
            validityStart = event.validity?.start,
            validityEnd = event.validity?.end,
            sessionsTotal = event.sessionsTotal,
            sessionsUsed = 0
        )
        productContractRepository.save(entity)

        queryUpdateEmitter.emit(
            FindAllProductContractsQuery::class.java,
            { true },
            ProductContractUpdatedUpdate(event.contractId.value.toString())
        )
    }

    @EventHandler
    fun on(event: ProductContractPausedEvent) {
        productContractRepository.findById(event.contractId.value).ifPresent { existing ->
            val updated = ProductContractEntity(
                contractId = existing.contractId,
                customerId = existing.customerId,
                productVariantId = existing.productVariantId,
                bookingId = existing.bookingId,
                status = ProductContractStatus.PAUSED,
                validityStart = existing.validityStart,
                validityEnd = existing.validityEnd,
                sessionsTotal = existing.sessionsTotal,
                sessionsUsed = existing.sessionsUsed
            )
            productContractRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllProductContractsQuery::class.java,
                { true },
                ProductContractUpdatedUpdate(event.contractId.value.toString())
            )
            queryUpdateEmitter.emit(
                FindProductContractByIdQuery::class.java,
                { query -> query.contractId == event.contractId },
                ProductContractUpdatedUpdate(event.contractId.value.toString())
            )
        }
    }

    @EventHandler
    fun on(event: ProductContractResumedEvent) {
        productContractRepository.findById(event.contractId.value).ifPresent { existing ->
            val updated = ProductContractEntity(
                contractId = existing.contractId,
                customerId = existing.customerId,
                productVariantId = existing.productVariantId,
                bookingId = existing.bookingId,
                status = ProductContractStatus.ACTIVE,
                validityStart = event.extendedValidity?.start,
                validityEnd = event.extendedValidity?.end,
                sessionsTotal = existing.sessionsTotal,
                sessionsUsed = existing.sessionsUsed
            )
            productContractRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllProductContractsQuery::class.java,
                { true },
                ProductContractUpdatedUpdate(event.contractId.value.toString())
            )
            queryUpdateEmitter.emit(
                FindProductContractByIdQuery::class.java,
                { query -> query.contractId == event.contractId },
                ProductContractUpdatedUpdate(event.contractId.value.toString())
            )
        }
    }

    @QueryHandler
    fun handle(query: FindProductContractByIdQuery): ProductContractView? =
        productContractRepository.findById(query.contractId.value).map { it.toProductContractView() }.orElse(null)

    @QueryHandler
    fun handle(query: FindAllProductContractsQuery): List<ProductContractView> =
        productContractRepository.findAll().map { it.toProductContractView() }

    fun findById(contractId: ProductContractId): ProductContractView? =
        productContractRepository.findById(contractId.value).map { it.toProductContractView() }.orElse(null)

    fun findAll(): List<ProductContractView> =
        productContractRepository.findAll().map { it.toProductContractView() }

    fun findByCustomerId(customerId: String): List<ProductContractView> =
        productContractRepository.findByCustomerId(UUID.fromString(customerId)).map { it.toProductContractView() }

    private fun ProductContractEntity.toProductContractView() = ProductContractView(
        contractId = this.contractId.toString(),
        customerId = this.customerId.toString(),
        productVariantId = this.productVariantId.toString(),
        bookingId = this.bookingId.toString(),
        status = this.status,
        validity = if (this.validityStart != null && this.validityEnd != null) {
            DateRange(this.validityStart!!, this.validityEnd!!)
        } else null,
        sessionsTotal = this.sessionsTotal,
        sessionsUsed = this.sessionsUsed,
        pauseHistory = emptyList() // Pause history not stored in entity for simplicity
    )
}

data class ProductContractView(
    val contractId: String,
    val customerId: String,
    val productVariantId: String,
    val bookingId: String,
    val status: ProductContractStatus,
    val validity: DateRange?,
    val sessionsTotal: Int?,
    val sessionsUsed: Int,
    val pauseHistory: List<PauseHistoryEntry>
)

data class PauseHistoryEntry(
    val pauseRange: DateRange,
    val reason: PauseReason
)
