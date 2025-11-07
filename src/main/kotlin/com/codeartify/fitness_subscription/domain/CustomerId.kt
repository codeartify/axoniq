package com.codeartify.fitness_subscription.domain

data class CustomerId(val value: String) {
    init {
        if (value.isBlank()) {
            throw InvalidCustomerIdException("CustomerId must not be blank")
        }
    }
    override fun toString(): String = value
}
