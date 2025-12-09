package ch.fitnesslab.domain.value

class BexioContactId(
    val value: Int,
) {
    init {
        require(value > 0) { "Bexio contact ID must be a positive integer" }
    }

    companion object {
        fun of(raw: Int): BexioContactId = BexioContactId(raw)
    }

    override fun toString(): String = value.toString()
}
