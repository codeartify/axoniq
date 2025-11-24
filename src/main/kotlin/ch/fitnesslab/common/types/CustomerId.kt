package ch.fitnesslab.common.types

import java.util.*

data class CustomerId(val value: UUID) {
    companion object {
        fun generate() = CustomerId(UUID.randomUUID())
        fun from(value: String) = CustomerId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
