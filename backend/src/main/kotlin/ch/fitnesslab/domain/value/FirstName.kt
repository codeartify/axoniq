package ch.fitnesslab.customers.domain.value

data class FirstName(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "First name cannot be blank" }
    }

    companion object {
        fun of(raw: String): FirstName = FirstName(raw.trim())
    }

    override fun toString(): String = value
}
