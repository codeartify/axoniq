package com.codeartify.fitness_subscription.domain

data class SignedUpForSubscriptionEvent(
    val subscriptionId: SubscriptionId,
    val customerId: CustomerId
)
