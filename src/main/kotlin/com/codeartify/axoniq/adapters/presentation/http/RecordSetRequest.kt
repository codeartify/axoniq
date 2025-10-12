package com.codeartify.axoniq.adapters.presentation.http

data class RecordSetRequest(
    val exerciseName: String,
    val repetitions: Int,
    val weight: Double
)
