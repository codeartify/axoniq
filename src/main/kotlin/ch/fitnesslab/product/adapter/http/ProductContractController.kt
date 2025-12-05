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

private fun toDto(productContractView: ProductContractView) =
    ProductContractDetailDto(
        contractId = productContractView.contractId,
        customerId = productContractView.customerId,
        productVariantId = productContractView.productVariantId,
        bookingId = productContractView.bookingId,
        status = productContractView.status.name,
        validity = productContractView.validity?.let { DateRangeDto(it.start, it.end) },
        sessionsTotal = productContractView.sessionsTotal,
        sessionsUsed = productContractView.sessionsUsed,
        pauseHistory =
            productContractView.pauseHistory.map {
                PauseHistoryEntryDto(
                    pauseRange = DateRangeDto(it.pauseRange.start, it.pauseRange.end),
                    reason = it.reason.name,
                )
            },
        canBePaused = productContractView.status == ProductContractStatus.ACTIVE,
    )

@RestController
@RequestMapping("/api/product-contracts")
class ProductContractController(
    private val productContractProjection: ProductContractProjection,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) : ProductContractsApi {
    @GetMapping("/{contractId}")
    override fun getContractById(
        @PathVariable contractId: String,
    ): ResponseEntity<ProductContractDetailDto> {
        val productContractId = ProductContractId.from(contractId)

        val contract = productContractProjection.findById(productContractId)

        return if (contract == null) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.ok(toDto(contract))
        }
    }

    @PostMapping("/{contractId}/pause")
    override fun pauseContract(
        @PathVariable contractId: String,
        @RequestBody pauseContractRequest: PauseContractRequest,
    ): ResponseEntity<Unit> {
        val subscriptionQuery = createFindAllProductContractsQuery()

        try {
            val command = pauseCommandFrom(contractId, pauseContractRequest)

            commandGateway.sendAndWait<Any>(command)

            waitForProjectionUpdate(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery?.close()
        }
    }

    private fun pauseCommandFrom(
        contractId: String,
        pauseContractRequest: PauseContractRequest
    ): PauseProductContractCommand {
        val productContractId = ProductContractId.from(contractId)

        val startDate = pauseContractRequest.startDate
        val endDate = pauseContractRequest.endDate
        val pauseRange = definePauseDuration(startDate, endDate)
        val pauseReason = toPauseReason(pauseContractRequest)

        val command =
            PauseProductContractCommand(
                contractId = productContractId,
                pauseRange = pauseRange,
                reason = pauseReason,
            )
        return command
    }

    private fun createFindAllProductContractsQuery(): SubscriptionQueryResult<MutableList<ProductContractView>, ProductContractUpdatedUpdate>? =
        queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java),
        )

    private fun toPauseReason(pauseContractRequest: PauseContractRequest): PauseReason =
        pauseContractRequest.reason.let {
            PauseReason.valueOf(it.name)
        }

    private fun definePauseDuration(
        startDate: LocalDate,
        endDate: LocalDate,
    ): DateRange =
        DateRange(
            start = LocalDate.parse(startDate.toString()),
            end = LocalDate.parse(endDate.toString()),
        )

    @PostMapping("/{contractId}/resume")
    override fun resumeContract(
        @PathVariable contractId: String,
    ): ResponseEntity<Unit> {
        val resumeSubscriptionQuery = createResumeSubscriptionQuery()

        try {
            val productContractId = ProductContractId.from(contractId)

            val command = ResumeProductContractCommand(productContractId)

            commandGateway.sendAndWait<Any>(command)

            waitForProjectionUpdate(resumeSubscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            resumeSubscriptionQuery?.close()
        }
    }

    private fun createResumeSubscriptionQuery(): SubscriptionQueryResult<MutableList<ProductContractView>, ProductContractUpdatedUpdate>? =
        queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java),
        )

    private fun waitForProjectionUpdate(
        subscriptionQuery: SubscriptionQueryResult<MutableList<ProductContractView>, ProductContractUpdatedUpdate>?,
    ) {
        subscriptionQuery
            ?.updates()
            ?.blockFirst(Duration.ofSeconds(5))
    }
}
