package ch.fitnesslab.contract.adapter.http

import ch.fitnesslab.contract.application.ContractProjection
import ch.fitnesslab.contract.application.ContractUpdatedUpdate
import ch.fitnesslab.contract.application.ContractView
import ch.fitnesslab.contract.application.FindAllContractsQuery
import ch.fitnesslab.contract.domain.commands.PauseContractCommand
import ch.fitnesslab.contract.domain.commands.ResumeContractCommand
import ch.fitnesslab.domain.ContractStatus
import ch.fitnesslab.domain.PauseReason
import ch.fitnesslab.domain.value.ContractId
import ch.fitnesslab.domain.value.DateRange
import ch.fitnesslab.domain.value.ProductVariantId
import ch.fitnesslab.generated.api.ContractsApi
import ch.fitnesslab.generated.model.ContractDetailDto
import ch.fitnesslab.generated.model.DateRangeDto
import ch.fitnesslab.generated.model.PauseContractRequest
import ch.fitnesslab.generated.model.PauseHistoryEntryDto
import ch.fitnesslab.product.application.ProductProjection
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class ContractController(
    private val contractProjection: ContractProjection,
    private val productProjection: ProductProjection,
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) : ContractsApi {
    override fun getContractsByCustomerId(customerId: String): ResponseEntity<List<ContractDetailDto>> {
        val contracts = contractProjection.findByCustomerId(customerId)
        val contractDtos = contracts.map { toDto(it) }
        return ResponseEntity.ok(contractDtos)
    }

    override fun getContractById(contractId: String): ResponseEntity<ContractDetailDto> {
        val contractId = ContractId.from(contractId)

        val contract = contractProjection.findById(contractId)

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
        val subscriptionQuery = createFindAllContractsQuery()

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
            val contractId = ContractId.from(contractId)

            val command = ResumeContractCommand(contractId)

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
    ): PauseContractCommand {
        val contractId = ContractId.from(contractId)

        val startDate = pauseContractRequest.startDate
        val endDate = pauseContractRequest.endDate
        val pauseRange = definePauseDuration(startDate, endDate)
        val pauseReason = toPauseReason(pauseContractRequest)

        val command =
            PauseContractCommand(
                contractId = contractId,
                pauseRange = pauseRange,
                reason = pauseReason,
            )
        return command
    }

    private fun createFindAllContractsQuery(): SubscriptionQueryResult<MutableList<ContractView>, ContractUpdatedUpdate> =
        queryGateway.subscriptionQuery(
            FindAllContractsQuery(),
            ResponseTypes.multipleInstancesOf(ContractView::class.java),
            ResponseTypes.instanceOf(ContractUpdatedUpdate::class.java),
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

    private fun createResumeSubscriptionQuery(): SubscriptionQueryResult<MutableList<ContractView>, ContractUpdatedUpdate> =
        queryGateway.subscriptionQuery(
            FindAllContractsQuery(),
            ResponseTypes.multipleInstancesOf(ContractView::class.java),
            ResponseTypes.instanceOf(ContractUpdatedUpdate::class.java),
        )

    private fun toDto(contractView: ContractView): ContractDetailDto {
        val productName = try {
            productProjection.findById(ProductVariantId.from(contractView.productVariantId))?.name
        } catch (e: Exception) {
            null
        }

        return ContractDetailDto(
            contractId = contractView.contractId,
            customerId = contractView.customerId,
            productVariantId = contractView.productVariantId,
            productName = productName,
            bookingId = contractView.bookingId,
            status = contractView.status.name,
            validity = contractView.validity?.let { DateRangeDto(it.start, it.end) },
            sessionsTotal = contractView.sessionsTotal,
            sessionsUsed = contractView.sessionsUsed,
            pauseHistory =
                contractView.pauseHistory.map {
                    PauseHistoryEntryDto(
                        pauseRange = DateRangeDto(it.pauseRange.start, it.pauseRange.end),
                        reason = it.reason.name,
                    )
                },
            canBePaused = contractView.status == ContractStatus.ACTIVE,
        )
    }
}
