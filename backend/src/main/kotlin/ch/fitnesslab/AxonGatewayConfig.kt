package ch.fitnesslab

import org.axonframework.common.configuration.AxonConfiguration
import org.axonframework.messaging.commandhandling.CommandBus
import org.axonframework.messaging.commandhandling.CommandPriorityCalculator
import org.axonframework.messaging.commandhandling.RoutingStrategy
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.commandhandling.gateway.ConvertingCommandGateway
import org.axonframework.messaging.commandhandling.gateway.DefaultCommandGateway
import org.axonframework.messaging.core.MessageTypeResolver
import org.axonframework.messaging.core.conversion.MessageConverter
import org.axonframework.messaging.queryhandling.QueryBus
import org.axonframework.messaging.queryhandling.QueryPriorityCalculator
import org.axonframework.messaging.queryhandling.gateway.DefaultQueryGateway
import org.axonframework.messaging.queryhandling.gateway.QueryGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonGatewayConfig {
    @Bean
    @ConditionalOnMissingBean
    fun commandGateway(axonConfiguration: AxonConfiguration): CommandGateway =
        ConvertingCommandGateway(
            DefaultCommandGateway(
                axonConfiguration.getComponent(CommandBus::class.java),
                axonConfiguration.getComponent(MessageTypeResolver::class.java),
                axonConfiguration.getComponent(CommandPriorityCalculator::class.java),
                axonConfiguration.getComponent(RoutingStrategy::class.java),
            ),
            axonConfiguration.getComponent(MessageConverter::class.java),
        )

    @Bean
    @ConditionalOnMissingBean
    fun queryGateway(axonConfiguration: AxonConfiguration): QueryGateway =
        DefaultQueryGateway(
            axonConfiguration.getComponent(QueryBus::class.java),
            axonConfiguration.getComponent(MessageTypeResolver::class.java),
            axonConfiguration.getComponent(QueryPriorityCalculator::class.java),
            axonConfiguration.getComponent(MessageConverter::class.java),
        )
}
