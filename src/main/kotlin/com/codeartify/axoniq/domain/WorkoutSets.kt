package com.codeartify.axoniq.domain

class WorkoutSets {

    private val sets = mutableListOf<WorkoutSet>()

    fun record(set: WorkoutSet) {
       this.sets.add(set)
    }
     fun count(): Int = sets.size

    fun findById(id: WorkoutSetId): WorkoutSet? = sets.firstOrNull { it.id == id }

    fun snapshot(): List<WorkoutSet> = sets.toList()

}
