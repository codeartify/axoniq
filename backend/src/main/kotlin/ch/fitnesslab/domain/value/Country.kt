package ch.fitnesslab.domain.value

data class Country(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Country cannot be blank" }
    }

    companion object {
        fun of(raw: String): Country = Country(raw.trim())
    }

    override fun toString(): String = value
}
