package ch.fitnesslab.domain.value

import java.util.UUID

data class ProductId(
    val value: UUID,
) {
    companion object {
        fun generate() = ProductId(UUID.randomUUID())

        fun from(value: String) = ProductId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
