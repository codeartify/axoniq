package com.codeartify.axoniq.domain

data class WorkoutSetId(val value: String) {
    companion object {
        fun create() = WorkoutSetId(java.util.UUID.randomUUID().toString())
    }
}

