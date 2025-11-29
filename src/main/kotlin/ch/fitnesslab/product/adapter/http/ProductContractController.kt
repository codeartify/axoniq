package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.common.types.DateRange
import ch.fitnesslab.common.types.ProductContractId
import ch.fitnesslab.generated.api.ProductContractsApi
import ch.fitnesslab.generated.model.DateRangeDto
import ch.fitnesslab.generated.model.PauseContractRequest
import ch.fitnesslab.generated.model.PauseHistoryEntryDto
import ch.fitnesslab.generated.model.ProductContractDetailDto
import ch.fitnesslab.product.application.FindAllProductContractsQuery
import ch.fitnesslab.product.application.ProductContractProjection
import ch.fitnesslab.product.application.ProductContractUpdatedUpdate
import ch.fitnesslab.product.application.ProductContractView
import ch.fitnesslab.product.domain.ProductContractStatus
import ch.fitnesslab.product.domain.commands.PauseProductContractCommand
import ch.fitnesslab.product.domain.commands.PauseReason
import ch.fitnesslab.product.domain.commands.ResumeProductContractCommand
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
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
) : ProductContractsApi {

    @GetMapping("/{contractId}")
    override fun getContractById(@PathVariable contractId: String): ResponseEntity<ProductContractDetailDto> {
        val contract = productContractProjection.findById(ProductContractId.from(contractId))
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(contract.toDto())
    }

    @PostMapping("/{contractId}/pause")
    override fun pauseContract(
        @PathVariable contractId: String, @RequestBody pauseContractRequest: PauseContractRequest
    ): ResponseEntity<Unit> {
        val subscriptionQuery = queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java)
        )

        try {
            val command = PauseProductContractCommand(
                contractId = ProductContractId.from(contractId), pauseRange = DateRange(
                    start = LocalDate.parse(pauseContractRequest.startDate.toString()),
                    end = LocalDate.parse(pauseContractRequest.endDate.toString())
                ), reason = pauseContractRequest.reason.let { PauseReason.valueOf(it.name) })
            commandGateway.sendAndWait<Any>(
                command
            )

            waitForProjectionUpdate(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    @PostMapping("/{contractId}/resume")
    override fun resumeContract(@PathVariable contractId: String): ResponseEntity<Unit> {
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
            waitForProjectionUpdate(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    private fun waitForProjectionUpdate(subscriptionQuery: SubscriptionQueryResult<MutableList<ProductContractView>, ProductContractUpdatedUpdate>?) {
        subscriptionQuery?.updates()?.blockFirst(Duration.ofSeconds(5))
    }
}

private fun ProductContractView.toDto() = ProductContractDetailDto(
    contractId = contractId,
    customerId = customerId,
    productVariantId = productVariantId,
    bookingId = bookingId,
    status = status.name, validity = validity?.let { DateRangeDto(it.start, it.end) },
    sessionsTotal = sessionsTotal,
    sessionsUsed = sessionsUsed,
    pauseHistory = pauseHistory.map {
        PauseHistoryEntryDto(
            pauseRange = DateRangeDto(it.pauseRange.start, it.pauseRange.end),
            reason = it.reason.name
        )
    },
    canBePaused = status == ProductContractStatus.ACTIVE
)
