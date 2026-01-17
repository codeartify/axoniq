package ch.fitnesslab.domain.value

import java.util.UUID

data class ContractId(
    val value: UUID,
) {
    companion object {
        fun generate() = ContractId(UUID.randomUUID())

        fun from(value: String) = ContractId(UUID.fromString(value))
    }

    override fun toString() = value.toString()
}
