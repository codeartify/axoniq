package com.codeartify.fitness_subscription.domain

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateSubscriptionCommand(
    @TargetAggregateIdentifier
    val subscriptionId: SubscriptionId,
    val customerId: CustomerId
)
