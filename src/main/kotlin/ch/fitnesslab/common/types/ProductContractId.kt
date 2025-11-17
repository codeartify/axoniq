package ch.fitnesslab.common.types

import java.util.UUID

@JvmInline
value class ProductContractId(val value: UUID) {
    companion object {
        fun generate() = ProductContractId(UUID.randomUUID())
        fun from(value: String) = ProductContractId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
