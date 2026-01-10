package ch.fitnesslab.domain.value

import java.util.UUID

data class ProductContractId(
    val value: UUID,
) {
    companion object {
        fun generate() = ProductContractId(UUID.randomUUID())

        fun from(value: String) = ProductContractId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
