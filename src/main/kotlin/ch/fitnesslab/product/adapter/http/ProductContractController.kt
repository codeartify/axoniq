package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.product.application.*
import ch.fitnesslab.product.domain.ProductContractStatus
import ch.fitnesslab.product.domain.commands.PauseProductContractCommand
import ch.fitnesslab.product.domain.commands.PauseReason
import ch.fitnesslab.product.domain.commands.ResumeProductContractCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.LocalDate

@RestController
@RequestMapping("/api/product-contracts")
class ProductContractController(
    private val productContractProjection: ProductContractProjection,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {

    @GetMapping("/{contractId}")
    fun getContractById(@PathVariable contractId: String): ResponseEntity<ProductContractDetailDto> {
        val contract = productContractProjection.findById(ProductContractId.from(contractId))
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(contract.toDetailDto())
    }

    @PostMapping("/{contractId}/pause")
    fun pauseContract(
        @PathVariable contractId: String,
        @RequestBody request: PauseContractRequest
    ): ResponseEntity<Void> {
        val subscriptionQuery = queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java)
        )

        try {
            commandGateway.sendAndWait<Any>(
                PauseProductContractCommand(
                    contractId = ProductContractId.from(contractId),
                    pauseRange = DateRange(
                        start = LocalDate.parse(request.startDate),
                        end = LocalDate.parse(request.endDate)
                    ),
                    reason = request.reason
                )
            )

            // Wait for projection update
            subscriptionQuery.updates().blockFirst(Duration.ofSeconds(5))

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    @PostMapping("/{contractId}/resume")
    fun resumeContract(@PathVariable contractId: String): ResponseEntity<Void> {
        val subscriptionQuery = queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java)
        )

        try {
            commandGateway.sendAndWait<Any>(
                ResumeProductContractCommand(
                    contractId = ProductContractId.from(contractId)
                )
            )

            // Wait for projection update
            subscriptionQuery.updates().blockFirst(Duration.ofSeconds(5))

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }
}

data class ProductContractDetailDto(
    val contractId: String,
    val customerId: String,
    val productVariantId: String,
    val bookingId: String,
    val status: String,
    val validity: DateRangeDto?,
    val sessionsTotal: Int?,
    val sessionsUsed: Int,
    val pauseHistory: List<PauseHistoryEntryDto>,
    val canBePaused: Boolean
)

data class DateRangeDto(
    val start: String,
    val end: String
)

data class PauseHistoryEntryDto(
    val pauseRange: DateRangeDto,
    val reason: String
)

data class PauseContractRequest(
    val startDate: String,
    val endDate: String,
    val reason: PauseReason
)

private fun ProductContractView.toDetailDto() = ProductContractDetailDto(
    contractId = contractId,
    customerId = customerId,
    productVariantId = productVariantId,
    bookingId = bookingId,
    status = status.name,
    validity = validity?.let { DateRangeDto(it.start.toString(), it.end.toString()) },
    sessionsTotal = sessionsTotal,
    sessionsUsed = sessionsUsed,
    pauseHistory = pauseHistory.map {
        PauseHistoryEntryDto(
            pauseRange = DateRangeDto(it.pauseRange.start.toString(), it.pauseRange.end.toString()),
            reason = it.reason.name
        )
    },
    canBePaused = status == ProductContractStatus.ACTIVE
)
