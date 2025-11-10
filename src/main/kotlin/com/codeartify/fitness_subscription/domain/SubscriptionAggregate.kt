package com.codeartify.fitness_subscription.domain

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class SubscriptionAggregate() {

    @AggregateIdentifier
    private lateinit var id: SubscriptionId
    private lateinit var customerId: CustomerId

    @CommandHandler
    constructor(command: SignUpForSubscriptionCommand) : this() {
        apply(
            SignedUpForSubscriptionEvent(
                subscriptionId = command.subscriptionId,
                customerId = command.customerId
            )
        )
    }

    @EventSourcingHandler
    fun on(event: SignedUpForSubscriptionEvent) {
        this.id = event.subscriptionId
        this.customerId = event.customerId
    }

    fun getId(): SubscriptionId = id

    fun getCustomerId(): CustomerId = customerId
}
