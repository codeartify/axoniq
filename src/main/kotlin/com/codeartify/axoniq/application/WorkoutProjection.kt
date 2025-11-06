package com.codeartify.axoniq.application

import com.codeartify.axoniq.adapter.data_access.WorkoutRepository
import com.codeartify.axoniq.domain.events.SetRecordedEvent
import org.axonframework.eventhandling.EventHandler

class WorkoutProjection(
    private val workoutRepository: WorkoutRepository
) {
    @EventHandler
    fun on(event: SetRecordedEvent) {
        workoutRepository.findById(event.workoutId)
    }
}
