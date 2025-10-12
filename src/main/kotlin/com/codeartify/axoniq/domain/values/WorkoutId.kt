package com.codeartify.axoniq.domain.values

import java.util.UUID

data class WorkoutId(val value: String) {
    companion object {
        fun create(): WorkoutId {
            return WorkoutId(UUID.randomUUID().toString())
        }
    }
}
