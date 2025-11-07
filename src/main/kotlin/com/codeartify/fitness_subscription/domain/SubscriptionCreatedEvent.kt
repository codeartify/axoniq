package com.codeartify.fitness_subscription.domain

data class SubscriptionCreatedEvent(
    val subscriptionId: SubscriptionId,
    val customerId: CustomerId
)
