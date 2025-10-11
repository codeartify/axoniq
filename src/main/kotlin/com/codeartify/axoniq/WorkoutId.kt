package com.codeartify.axoniq

data class WorkoutId(val value: String) {
    companion object {
        fun create(): WorkoutId {
            return WorkoutId(java.util.UUID.randomUUID().toString())
        }
    }
}
