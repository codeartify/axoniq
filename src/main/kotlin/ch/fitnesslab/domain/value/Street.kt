package ch.fitnesslab.domain.value

data class Street(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Street cannot be blank" }
    }

    companion object {
        fun of(raw: String): Street = Street(raw.trim())
    }

    override fun toString(): String = value
}
