package ch.fitnesslab.domain.value

data class HouseNumber(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "House number cannot be blank" }
    }

    companion object {
        fun of(raw: String): HouseNumber = HouseNumber(raw.trim())
    }

    override fun toString(): String = value
}
