package ch.fitnesslab.product.application

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.product.domain.ProductContractStatus
import ch.fitnesslab.product.domain.commands.PauseReason
import ch.fitnesslab.product.domain.events.ProductContractSignedEvent
import ch.fitnesslab.product.domain.events.ProductContractPausedEvent
import ch.fitnesslab.product.domain.events.ProductContractResumedEvent
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ProductContractProjection {

    private val contracts = ConcurrentHashMap<ProductContractId, ProductContractView>()

    @EventHandler
    fun on(event: ProductContractSignedEvent) {
        contracts[event.contractId] = ProductContractView(
            contractId = event.contractId.toString(),
            customerId = event.customerId.toString(),
            productVariantId = event.productVariantId.toString(),
            bookingId = event.bookingId.toString(),
            status = event.status,
            validity = event.validity,
            sessionsTotal = event.sessionsTotal,
            sessionsUsed = 0,
            pauseHistory = emptyList()
        )
    }

    @EventHandler
    fun on(event: ProductContractPausedEvent) {
        contracts.computeIfPresent(event.contractId) { _, contract ->
            contract.copy(
                status = ProductContractStatus.PAUSED,
                pauseHistory = contract.pauseHistory + PauseHistoryEntry(
                    pauseRange = event.pauseRange,
                    reason = event.reason
                )
            )
        }
    }

    @EventHandler
    fun on(event: ProductContractResumedEvent) {
        contracts.computeIfPresent(event.contractId) { _, contract ->
            contract.copy(
                status = ProductContractStatus.ACTIVE,
                validity = event.extendedValidity
            )
        }
    }

    fun findById(contractId: ProductContractId): ProductContractView? = contracts[contractId]

    fun findAll(): List<ProductContractView> = contracts.values.toList()

    fun findByCustomerId(customerId: String): List<ProductContractView> =
        contracts.values.filter { it.customerId == customerId }
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
