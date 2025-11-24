package ch.fitnesslab.common.types

import java.util.UUID


data class BookingId(val value: UUID) {
    companion object {
        fun generate() = BookingId(UUID.randomUUID())
        fun from(value: String) = BookingId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
