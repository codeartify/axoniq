package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductContractId
import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.generated.api.ProductContractsApi
import ch.fitnesslab.generated.model.DateRangeDto
import ch.fitnesslab.generated.model.PauseContractRequest
import ch.fitnesslab.generated.model.PauseHistoryEntryDto
import ch.fitnesslab.generated.model.ProductContractDetailDto
import ch.fitnesslab.product.application.FindAllProductContractsQuery
import ch.fitnesslab.product.application.ProductContractProjection
import ch.fitnesslab.product.application.ProductContractUpdatedUpdate
import ch.fitnesslab.product.application.ProductContractView
import ch.fitnesslab.product.application.ProductProjection
import ch.fitnesslab.product.domain.ProductContractStatus
import ch.fitnesslab.product.domain.commands.PauseProductContractCommand
import ch.fitnesslab.product.domain.commands.PauseReason
import ch.fitnesslab.product.domain.commands.ResumeProductContractCommand
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class ProductContractController(
    private val productContractProjection: ProductContractProjection,
    private val productProjection: ProductProjection,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) : ProductContractsApi {
    override fun getContractsByCustomerId(customerId: String): ResponseEntity<List<ProductContractDetailDto>> {
        val contracts = productContractProjection.findByCustomerId(customerId)
        val contractDtos = contracts.map { toDto(it) }
        return ResponseEntity.ok(contractDtos)
    }

    override fun getContractById(contractId: String): ResponseEntity<ProductContractDetailDto> {
        val productContractId = ProductContractId.from(contractId)

        val contract = productContractProjection.findById(productContractId)

        return if (contract == null) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.ok(toDto(contract))
        }
    }

    override fun pauseContract(
        contractId: String,
        pauseContractRequest: PauseContractRequest,
    ): ResponseEntity<Unit> {
        val subscriptionQuery = createFindAllProductContractsQuery()

        try {
            val command = pauseCommandFrom(contractId, pauseContractRequest)

            commandGateway.sendAndWait<Any>(command)

            waitForUpdateOf(subscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            subscriptionQuery.close()
        }
    }

    override fun resumeContract(contractId: String): ResponseEntity<Unit> {
        val resumeSubscriptionQuery = createResumeSubscriptionQuery()

        try {
            val productContractId = ProductContractId.from(contractId)

            val command = ResumeProductContractCommand(productContractId)

            commandGateway.sendAndWait<Any>(command)

            waitForUpdateOf(resumeSubscriptionQuery)

            return ResponseEntity.ok().build()
        } finally {
            resumeSubscriptionQuery.close()
        }
    }

    private fun pauseCommandFrom(
        contractId: String,
        pauseContractRequest: PauseContractRequest,
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

    private fun createFindAllProductContractsQuery(): SubscriptionQueryResult<MutableList<ProductContractView>, ProductContractUpdatedUpdate> =
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

    private fun createResumeSubscriptionQuery(): SubscriptionQueryResult<MutableList<ProductContractView>, ProductContractUpdatedUpdate> =
        queryGateway.subscriptionQuery(
            FindAllProductContractsQuery(),
            ResponseTypes.multipleInstancesOf(ProductContractView::class.java),
            ResponseTypes.instanceOf(ProductContractUpdatedUpdate::class.java),
        )

    private fun toDto(productContractView: ProductContractView): ProductContractDetailDto {
        val productName = try {
            productProjection.findById(ProductVariantId.from(productContractView.productVariantId))?.name
        } catch (e: Exception) {
            null
        }

        return ProductContractDetailDto(
            contractId = productContractView.contractId,
            customerId = productContractView.customerId,
            productVariantId = productContractView.productVariantId,
            productName = productName,
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
    }
}
