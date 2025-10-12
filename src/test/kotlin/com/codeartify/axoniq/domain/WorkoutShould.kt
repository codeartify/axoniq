package com.codeartify.axoniq.domain

import com.codeartify.axoniq.domain.WorkoutStatus.*
import com.codeartify.axoniq.domain.commands.FinishWorkoutCommand
import com.codeartify.axoniq.domain.commands.RecordSetCommand
import com.codeartify.axoniq.domain.commands.StartWorkoutCommand
import com.codeartify.axoniq.domain.events.SetRecordedEvent
import com.codeartify.axoniq.domain.events.WorkoutFinishedEvent
import com.codeartify.axoniq.domain.events.WorkoutStartedEvent
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorkoutShould {

    private lateinit var fixture: AggregateTestFixture<Workout>

    @BeforeEach
    fun setUp() {
        fixture = AggregateTestFixture(Workout::class.java)
    }

    @Test
    fun `be started`() {
        val id = WorkoutId.create()

        fixture.givenNoPriorActivity()
            .`when`(StartWorkoutCommand(id))
            .expectEvents(WorkoutStartedEvent(id))
            .expectState { assertThat(it.getId()).isEqualTo(id) }
    }

    @Test
    fun `be finished`() {
        val id = WorkoutId.create()
        fixture.given(WorkoutStartedEvent(id))
            .`when`(FinishWorkoutCommand(id))
            .expectEvents(WorkoutFinishedEvent(id))
            .expectState { assertThat(it.getId()).isEqualTo(id) }
    }

    @Test
    fun `be possible to record sets after starting the workout`() {
        val id = WorkoutId.create()
        fixture.given(WorkoutStartedEvent(id))
            .`when`(RecordSetCommand(workoutId = id))
            .expectEvents(SetRecordedEvent(workoutId = id))
    }

}
