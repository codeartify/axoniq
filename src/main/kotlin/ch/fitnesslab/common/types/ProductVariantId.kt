package ch.fitnesslab.common.types

import java.util.UUID

@JvmInline
value class ProductVariantId(val value: UUID) {
    companion object {
        fun generate() = ProductVariantId(UUID.randomUUID())
        fun from(value: String) = ProductVariantId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
