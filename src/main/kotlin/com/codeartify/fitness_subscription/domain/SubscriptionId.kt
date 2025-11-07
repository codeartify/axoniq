package com.codeartify.fitness_subscription.domain

data class SubscriptionId(val value: String) {
    init {
        if (value.isBlank()) {
            throw InvalidSubscriptionIdException("SubscriptionId must not be blank")
        }
    }

    override fun toString(): String = value
}
