package ch.fitnesslab.customers.application.use_case

import ch.fitnesslab.customers.application.CustomerUpdatedUpdate
import ch.fitnesslab.customers.application.FindAllCustomersQuery
import ch.fitnesslab.customers.domain.commands.RegisterCustomerCommand
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.generated.model.CustomerView
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Service

@Service
class RegisterCustomerUseCase(
    private val queryGateway: QueryGateway,
    private val commandGateway: CommandGateway,
) {
    fun execute(command: RegisterCustomerCommand): CustomerId {
        val subscriptionQuery =
            queryGateway.subscriptionQuery(
                FindAllCustomersQuery(),
                ResponseTypes.multipleInstancesOf(CustomerView::class.java),
                ResponseTypes.instanceOf(CustomerUpdatedUpdate::class.java),
            )

        try {
            val customerId = commandGateway.sendAndWait<CustomerId>(command)

            waitForUpdateOf(subscriptionQuery)

            return customerId
        } finally {
            subscriptionQuery.close()
        }
    }
}
