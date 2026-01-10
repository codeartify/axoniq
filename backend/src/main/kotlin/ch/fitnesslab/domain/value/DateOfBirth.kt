package ch.fitnesslab.customers.domain.value

import java.time.LocalDate
import java.time.Period

/**
 * Immutable date of birth with invariants:
 * - must be in the past
 * - must correspond to an age >= 16 (using today's date, or a provided 'asOf' date)
 */
data class DateOfBirth(
    val value: LocalDate,
) {
    init {
        val today = LocalDate.now()
        require(value.isBefore(today)) { "Date of birth must be in the past" }
        require(age(today).value >= 16) { "Customer must be at least 16 years old" }
    }

    fun age(asOf: LocalDate = LocalDate.now()): Age {
        require(!asOf.isBefore(value)) { "Age cannot be calculated with a date before date of birth" }
        val years = Period.between(value, asOf).years
        return Age.of(years)
    }

    companion object {
        fun of(date: LocalDate): DateOfBirth = DateOfBirth(date)
    }

    override fun toString(): String = value.toString()
}
