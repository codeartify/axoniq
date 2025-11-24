package ch.fitnesslab.common.types

import java.util.*

data class InvoiceId(val value: UUID) {
    companion object {
        fun generate() = InvoiceId(UUID.randomUUID())
        fun from(value: String) = InvoiceId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
