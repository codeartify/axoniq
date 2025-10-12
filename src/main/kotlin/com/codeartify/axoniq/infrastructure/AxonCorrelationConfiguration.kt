package com.codeartify.axoniq.infrastructure

import org.axonframework.messaging.correlation.MessageOriginProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonCorrelationConfiguration {
    @Bean
    fun messageOriginProvider(): MessageOriginProvider = MessageOriginProvider()
}
