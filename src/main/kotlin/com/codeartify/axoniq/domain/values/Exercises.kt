package com.codeartify.axoniq.domain.values

import com.codeartify.axoniq.domain.Exercise
import com.codeartify.axoniq.domain.Set

class Exercises {

    private val exercises = mutableMapOf<ExerciseName, Exercise>()

    fun record(exerciseName: ExerciseName, set: Set) {
        val exercise = exercises.getOrPut(exerciseName) { Exercise(exerciseName) }

        exercise.add(set)
    }


}
