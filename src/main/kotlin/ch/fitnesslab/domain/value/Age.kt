package ch.fitnesslab.customers.domain.value

data class Age(
    val value: Int,
) {
    init {
        require(value >= 0) { "Age cannot be negative" }
    }

    companion object {
        fun of(years: Int): Age = Age(years)
    }

    override fun toString(): String = value.toString()
}
