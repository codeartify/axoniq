package com.codeartify.axoniq.domain

import com.codeartify.axoniq.domain.commands.FinishWorkoutCommand
import com.codeartify.axoniq.domain.commands.RecordSetCommand
import com.codeartify.axoniq.domain.commands.StartWorkoutCommand
import com.codeartify.axoniq.domain.events.SetRecordedEvent
import com.codeartify.axoniq.domain.events.WorkoutFinishedEvent
import com.codeartify.axoniq.domain.events.WorkoutStartedEvent
import com.codeartify.axoniq.domain.exception.FinishingWorkoutFailedException
import com.codeartify.axoniq.domain.exception.RecordingSetFailedException
import com.codeartify.axoniq.domain.values.Exercises
import com.codeartify.axoniq.domain.values.WorkoutId
import com.codeartify.axoniq.domain.values.WorkoutStatus.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.serialization.Revision
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
@Revision("1.0.0")
class Workout() {

    @AggregateIdentifier
    private lateinit var id: WorkoutId

    private var status = STARTED

    private val exercises = Exercises()

    @CommandHandler
    constructor(startWorkoutCommand: StartWorkoutCommand) : this() {
        apply(WorkoutStartedEvent(startWorkoutCommand.id))
    }

    @EventSourcingHandler
    fun onStarted(event: WorkoutStartedEvent) {
        this.id = event.id
    }

    @CommandHandler(payloadType = FinishWorkoutCommand::class)
    fun finish() {
        if (!isStarted()) {
            throw FinishingWorkoutFailedException("Workout cannot be finished if it wasn't started")
        }
        apply(WorkoutFinishedEvent(id = this.id))
    }

    @EventSourcingHandler(payloadType = WorkoutFinishedEvent::class)
    fun onFinished() {
        this.status = FINISHED
    }

    @CommandHandler
    fun recordSet(recordSetCommand: RecordSetCommand) {
        if (isFinished()) {
            throw RecordingSetFailedException("Cannot record sets on a finished workout")
        }
        apply(
            SetRecordedEvent(
                workoutId = id,
                setId = recordSetCommand.setId,
                exerciseName = recordSetCommand.exerciseName,
                repetitions = recordSetCommand.repetitions,
                weight = recordSetCommand.weight
            )
        )
    }


    @EventSourcingHandler()
    fun onSetRecorded(setRecordedEvent: SetRecordedEvent) {
        this.exercises.record(
            setRecordedEvent.exerciseName,
            Set(
                setRecordedEvent.setId,
                setRecordedEvent.repetitions,
                setRecordedEvent.weight
            )
        )
    }


    fun isStarted(): Boolean = status == STARTED

    fun isFinished(): Boolean = status == FINISHED

    fun getId() = id.copy()



}
