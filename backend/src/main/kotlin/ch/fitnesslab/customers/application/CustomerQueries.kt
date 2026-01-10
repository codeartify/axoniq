package ch.fitnesslab.customers.application

import ch.fitnesslab.domain.value.CustomerId

data class FindAllCustomersQuery(
    val timestamp: Long = System.currentTimeMillis(),
)

data class FindCustomerByIdQuery(
    val customerId: CustomerId,
)

data class CustomerUpdatedUpdate(
    val customerId: String,
)
