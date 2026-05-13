package ch.fitnesslab.customers.application.use_case

import ch.fitnesslab.customers.application.FindAllCustomersQuery
import ch.fitnesslab.customers.domain.commands.RegisterCustomerCommand
import ch.fitnesslab.domain.value.CustomerId
import ch.fitnesslab.utils.waitForUpdateOf
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.queryhandling.gateway.QueryGateway
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
                Any::class.java,
            )

        try {
            waitForUpdateOf(subscriptionQuery) {
                commandGateway.sendAndWait(command)
            }

            return command.customerId
        } finally {
        }
    }
}
