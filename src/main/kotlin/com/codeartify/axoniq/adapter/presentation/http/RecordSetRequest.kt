package com.codeartify.axoniq.adapter.presentation.http

data class RecordSetRequest(
    val exerciseName: String,
    val repetitions: Int,
    val weight: Double
)
