package ch.fitnesslab.domain.value

data class PostalCode(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Postal code cannot be blank" }
    }

    companion object {
        fun of(raw: String): PostalCode = PostalCode(raw.trim())
    }

    override fun toString(): String = value
}
