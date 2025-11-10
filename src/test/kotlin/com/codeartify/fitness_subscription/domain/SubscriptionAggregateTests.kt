package com.codeartify.fitness_subscription.domain

import org.assertj.core.api.Assertions.assertThat
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubscriptionAggregateShould {

    private lateinit var fixture: AggregateTestFixture<SubscriptionAggregate>

    @BeforeEach
    fun setUp() {
        fixture = AggregateTestFixture(SubscriptionAggregate::class.java)
    }

    @Test
    fun `sign up for a new subscription`() {
        val subscriptionId = SubscriptionId("sub-1")
        val customerId = CustomerId("cust-1")

        fixture.givenNoPriorActivity()
            .`when`(
                SignUpForSubscriptionCommand(
                    subscriptionId = subscriptionId,
                    customerId = customerId
                )
            )
            .expectEvents(
                SignedUpForSubscriptionEvent(
                    subscriptionId = subscriptionId,
                    customerId = customerId
                )
            )
            .expectState { aggregate ->
                assertThat(aggregate.getId()).isEqualTo(subscriptionId)
                assertThat(aggregate.getCustomerId()).isEqualTo(customerId)
            }
    }
}
