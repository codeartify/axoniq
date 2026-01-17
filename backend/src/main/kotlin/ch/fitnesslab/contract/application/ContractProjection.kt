package ch.fitnesslab.contract.application

import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.ContractStatus
import ch.fitnesslab.contract.domain.events.ContractPausedEvent
import ch.fitnesslab.contract.domain.events.ContractResumedEvent
import ch.fitnesslab.contract.domain.events.ContractSignedEvent
import ch.fitnesslab.contract.infrastructure.ContractEntity
import ch.fitnesslab.contract.infrastructure.ContractRepository
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.*

@ProcessingGroup("contracts")
@Component
class ContractProjection(
    private val contractRepository: ContractRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter,
) {
    @EventHandler
    fun on(event: ContractSignedEvent) {
        val entity =
            ContractEntity(
                contractId = event.contractId.value,
                customerId = event.customerId.value,
                productVariantId = event.productVariantId.value,
                bookingId = event.bookingId.value,
                status = ContractStatus.ACTIVE,
                validityStart = event.validity?.start,
                validityEnd = event.validity?.end,
                sessionsTotal = event.sessionsTotal,
                sessionsUsed = 0,
            )
        contractRepository.save(entity)

        queryUpdateEmitter.emit(
            FindAllContractsQuery::class.java,
            { true },
            ContractUpdatedUpdate(event.contractId.value.toString()),
        )
    }

    @EventHandler
    fun on(event: ContractPausedEvent) {
        contractRepository.findById(event.contractId.value).ifPresent { existing ->
            val updated =
                ContractEntity(
                    contractId = existing.contractId,
                    customerId = existing.customerId,
                    productVariantId = existing.productVariantId,
                    bookingId = existing.bookingId,
                    status = ContractStatus.PAUSED,
                    validityStart = existing.validityStart,
                    validityEnd = existing.validityEnd,
                    sessionsTotal = existing.sessionsTotal,
                    sessionsUsed = existing.sessionsUsed,
                    pauseHistory = existing.pauseHistory + PauseHistoryEntry(event.pauseRange, event.reason),
                )
            contractRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllContractsQuery::class.java,
                { true },
                ContractUpdatedUpdate(event.contractId.value.toString()),
            )
            queryUpdateEmitter.emit(
                FindContractByIdQuery::class.java,
                { query -> query.contractId == event.contractId },
                ContractUpdatedUpdate(event.contractId.value.toString()),
            )
        }
    }

    @EventHandler
    fun on(event: ContractResumedEvent) {
        contractRepository.findById(event.contractId.value).ifPresent { existing ->
            val updated =
                ContractEntity(
                    contractId = existing.contractId,
                    customerId = existing.customerId,
                    productVariantId = existing.productVariantId,
                    bookingId = existing.bookingId,
                    status = ContractStatus.ACTIVE,
                    validityStart = event.extendedValidity.start,
                    validityEnd = event.extendedValidity.end,
                    sessionsTotal = existing.sessionsTotal,
                    sessionsUsed = existing.sessionsUsed,
                    pauseHistory = existing.pauseHistory,
                )
            contractRepository.save(updated)

            queryUpdateEmitter.emit(
                FindAllContractsQuery::class.java,
                { true },
                ContractUpdatedUpdate(event.contractId.value.toString()),
            )
            queryUpdateEmitter.emit(
                FindContractByIdQuery::class.java,
                { query -> query.contractId == event.contractId },
                ContractUpdatedUpdate(event.contractId.value.toString()),
            )
        }
    }

    @QueryHandler
    fun handle(query: FindContractByIdQuery): ContractView? =
        contractRepository.findById(query.contractId.value).map { toContractView(it) }.orElse(null)

    @QueryHandler
    fun handle(query: FindAllContractsQuery): List<ContractView> =
        contractRepository.findAll().map { toContractView(it) }

    fun findById(contractId: ContractId): ContractView? =
        contractRepository.findById(contractId.value).map { toContractView(it) }.orElse(null)

    fun findAll(): List<ContractView> = contractRepository.findAll().map { toContractView(it) }

    fun findByCustomerId(customerId: String): List<ContractView> =
        contractRepository.findByCustomerId(UUID.fromString(customerId)).map { toContractView(it) }

    private fun toContractView(entity: ContractEntity) =
        ContractView(
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
