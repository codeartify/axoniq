package ch.fitnesslab.customers.domain.value

data class EmailAddress(
    val value: String,
) {
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"

    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.matches(Regex(EMAIL_REGEX))) { "Invalid email format" }
    }

    companion object {
        fun of(raw: String): EmailAddress = EmailAddress(raw.trim())
    }

    override fun toString(): String = value
}
