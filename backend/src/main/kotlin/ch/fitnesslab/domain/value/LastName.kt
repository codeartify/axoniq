package ch.fitnesslab.customers.domain.value

data class LastName(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Last name cannot be blank" }
    }

    companion object {
        fun of(raw: String): LastName = LastName(raw.trim())
    }

    override fun toString(): String = value
}
