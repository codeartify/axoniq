package com.codeartify.axoniq.domain

import com.codeartify.axoniq.domain.commands.FinishWorkoutCommand
import com.codeartify.axoniq.domain.commands.RecordSetCommand
import com.codeartify.axoniq.domain.commands.StartWorkoutCommand
import com.codeartify.axoniq.domain.events.SetRecordedEvent
import com.codeartify.axoniq.domain.events.WorkoutFinishedEvent
import com.codeartify.axoniq.domain.events.WorkoutStartedEvent
import com.codeartify.axoniq.domain.exception.FinishingWorkoutFailedException
import com.codeartify.axoniq.domain.exception.RecordingSetFailedException
import com.codeartify.axoniq.domain.values.ExerciseName
import com.codeartify.axoniq.domain.values.Repetitions
import com.codeartify.axoniq.domain.values.SetId
import com.codeartify.axoniq.domain.values.Weight
import com.codeartify.axoniq.domain.values.WorkoutId
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
    fun `be possible to start`() {
        val id = WorkoutId.create()

        fixture.givenNoPriorActivity()
            .`when`(StartWorkoutCommand(id))
            .expectEvents(WorkoutStartedEvent(id))
            .expectState { assertThat(it.getId()).isEqualTo(id) }
    }

    @Test
    fun `be possible to finish`() {
        val id = WorkoutId.create()
        fixture.given(WorkoutStartedEvent(id))
            .`when`(FinishWorkoutCommand(id))
            .expectEvents(WorkoutFinishedEvent(id))
            .expectState { assertThat(it.getId()).isEqualTo(id) }
    }

    @Test
    fun `not be possible to finish twice`() {
        val id = WorkoutId.create()
        fixture.given(WorkoutStartedEvent(id))
            .andGiven(WorkoutFinishedEvent(id))
            .`when`(FinishWorkoutCommand(id))
            .expectException(FinishingWorkoutFailedException::class.java)
            .expectExceptionMessage("Workout cannot be finished if it wasn't started")
            .expectNoEvents()
    }

    @Test
    fun `be possible to record sets after starting the workout`() {
        val id = WorkoutId.create()
        val setId = SetId.create()
        fixture.given(WorkoutStartedEvent(id))
            .`when`(
                RecordSetCommand(
                    setId = setId,
                    workoutId = id,
                    exerciseName = ExerciseName("Bench Press"),
                    repetitions = Repetitions(10),
                    weight = Weight(100.0)
                )
            )
            .expectEvents(
                SetRecordedEvent(
                    setId = setId,
                    workoutId = id,
                    exerciseName = ExerciseName("Bench Press"),
                    repetitions = Repetitions(10),
                    weight = Weight(100.0),
                )
            )
    }

    @Test
    fun `not be possible to record sets after finishing the workout`() {
        val id = WorkoutId.create()
        val setId = SetId.create()

        fixture
            .given(WorkoutStartedEvent(id))
            .andGiven(WorkoutFinishedEvent(id))
            .`when`(
                RecordSetCommand(
                    setId = setId,
                    workoutId = id,
                    exerciseName = ExerciseName("Bench Press"),
                    repetitions = Repetitions(10),
                    weight = Weight(100.0)
                )
            )
            .expectException(RecordingSetFailedException::class.java)
            .expectExceptionMessage("Cannot record sets on a finished workout")
            .expectNoEvents()
    }

}
