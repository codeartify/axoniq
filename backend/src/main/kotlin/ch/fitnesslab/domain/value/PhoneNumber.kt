package ch.fitnesslab.customers.domain.value

data class PhoneNumber(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Phone number cannot be blank if provided" }
    }

    companion object {
        fun of(raw: String?): PhoneNumber? =
            raw
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { PhoneNumber(it) }
    }

    override fun toString(): String = value
}
