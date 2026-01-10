package ch.fitnesslab.product.application

import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductContractId
import ch.fitnesslab.product.domain.ProductContractStatus
import ch.fitnesslab.product.domain.events.ProductContractPausedEvent
import ch.fitnesslab.product.domain.events.ProductContractResumedEvent
import ch.fitnesslab.product.domain.events.ProductContractSignedEvent
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
    private val queryUpdateEmitter: QueryUpdateEmitter,
) {
    @EventHandler
    fun on(event: ProductContractSignedEvent) {
        val entity =
            ProductContractEntity(
                contractId = event.contractId.value,
                customerId = event.customerId.value,
                productVariantId = event.productVariantId.value,
                bookingId = event.bookingId.value,
                status = ProductContractStatus.ACTIVE,
                validityStart = event.validity?.start,
                validityEnd = event.validity?.end,
                sessionsTotal = event.sessionsTotal,
                sessionsUsed = 0,
            )
        productContractRepository.save(entity)

        queryUpdateEmitter.emit(
            FindAllProductContractsQuery::class.java,
            { true },
            ProductContractUpdatedUpdate(event.contractId.value.toString()),
        )
    }

    @EventHandler
    fun on(event: ProductContractPausedEvent) {
        productContractRepository.findById(event.contractId.value).ifPresent { existing ->
            val updated =
                ProductContractEntity(
                    contractId = existing.contractId,
                    customerId = existing.customerId,
                    productVariantId = existing.productVariantId,
                    bookingId = existing.bookingId,
                    status = ProductContractStatus.PAUSED,
                    validityStart = existing.validityStart,
                    validityEnd = existing.validityEnd,
                    sessionsTotal = existing.sessionsTotal,
                    sessionsUsed = existing.sessionsUsed,
                    pauseHistory = existing.pauseHistory + PauseHistoryEntry(event.pauseRange, event.reason),
                )
            productContractRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllProductContractsQuery::class.java,
                { true },
                ProductContractUpdatedUpdate(event.contractId.value.toString()),
            )
            queryUpdateEmitter.emit(
                FindProductContractByIdQuery::class.java,
                { query -> query.contractId == event.contractId },
                ProductContractUpdatedUpdate(event.contractId.value.toString()),
            )
        }
    }

    @EventHandler
    fun on(event: ProductContractResumedEvent) {
        productContractRepository.findById(event.contractId.value).ifPresent { existing ->
            val updated =
                ProductContractEntity(
                    contractId = existing.contractId,
                    customerId = existing.customerId,
                    productVariantId = existing.productVariantId,
                    bookingId = existing.bookingId,
                    status = ProductContractStatus.ACTIVE,
                    validityStart = event.extendedValidity.start,
                    validityEnd = event.extendedValidity.end,
                    sessionsTotal = existing.sessionsTotal,
                    sessionsUsed = existing.sessionsUsed,
                    pauseHistory = existing.pauseHistory,
                )
            productContractRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllProductContractsQuery::class.java,
                { true },
                ProductContractUpdatedUpdate(event.contractId.value.toString()),
            )
            queryUpdateEmitter.emit(
                FindProductContractByIdQuery::class.java,
                { query -> query.contractId == event.contractId },
                ProductContractUpdatedUpdate(event.contractId.value.toString()),
            )
        }
    }

    @QueryHandler
    fun handle(query: FindProductContractByIdQuery): ProductContractView? =
        productContractRepository.findById(query.contractId.value).map { toProductContractView(it) }.orElse(null)

    @QueryHandler
    fun handle(query: FindAllProductContractsQuery): List<ProductContractView> =
        productContractRepository.findAll().map { toProductContractView(it) }

    fun findById(contractId: ProductContractId): ProductContractView? =
        productContractRepository.findById(contractId.value).map { toProductContractView(it) }.orElse(null)

    fun findAll(): List<ProductContractView> = productContractRepository.findAll().map { toProductContractView(it) }

    fun findByCustomerId(customerId: String): List<ProductContractView> =
        productContractRepository.findByCustomerId(UUID.fromString(customerId)).map { toProductContractView(it) }

    private fun toProductContractView(entity: ProductContractEntity) =
        ProductContractView(
            contractId = entity.contractId.toString(),
            customerId = entity.customerId.toString(),
            productVariantId = entity.productVariantId.toString(),
            bookingId = entity.bookingId.toString(),
            status = entity.status,
            validity =
                if (entity.validityStart != null && entity.validityEnd != null) {
                    DateRange(entity.validityStart!!, entity.validityEnd!!)
                } else {
                    null
                },
            sessionsTotal = entity.sessionsTotal,
            sessionsUsed = entity.sessionsUsed,
            pauseHistory = entity.pauseHistory,
        )
}
