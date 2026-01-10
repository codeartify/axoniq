package ch.fitnesslab.membership.domain

import java.time.LocalDate

data class DueDate(
    val value: LocalDate,
) {
    companion object {
        fun inDays(dueDateInDays: Long): DueDate = DueDate(value = LocalDate.now().plusDays(dueDateInDays))
    }
}
