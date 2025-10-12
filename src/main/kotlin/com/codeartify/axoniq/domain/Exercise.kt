package com.codeartify.axoniq.domain

import com.codeartify.axoniq.domain.values.ExerciseName

data class Exercise(private val exerciseName: ExerciseName) {
    private val sets = mutableListOf<Set>()

    fun add(set: Set) {
        sets.add(set)
    }
}
