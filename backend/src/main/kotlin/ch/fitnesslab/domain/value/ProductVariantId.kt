package ch.fitnesslab.domain.value

import java.util.UUID

data class ProductVariantId(
    val value: UUID,
) {
    companion object {
        fun generate() = ProductVariantId(UUID.randomUUID())

        fun from(value: String) = ProductVariantId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
