package ch.fitnesslab.common.types

import java.time.LocalDate

data class DateRange(
    val start: LocalDate,
    val end: LocalDate,
) {
    companion object {
        fun toRange(
            startDate: LocalDate,
            durationInMonths: Int,
        ): DateRange =
            DateRange(
                start = startDate,
                end = startDate.plusMonths((durationInMonths.toLong())),
            )
    }

    init {
        require(!end.isBefore(start)) { "End date must not be before start date" }
    }

    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    fun extendBy(days: Long): DateRange = copy(end = end.plusDays(days))
}
