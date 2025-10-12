package com.codeartify.axoniq.domain

import com.codeartify.axoniq.domain.WorkoutStatus.FINISHED
import com.codeartify.axoniq.domain.WorkoutStatus.STARTED
import com.codeartify.axoniq.domain.commands.FinishWorkoutCommand
import com.codeartify.axoniq.domain.commands.StartWorkoutCommand
import com.codeartify.axoniq.domain.events.WorkoutFinishedEvent
import com.codeartify.axoniq.domain.events.WorkoutStartedEvent
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

    private var status: WorkoutStatus = STARTED

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
        if (status != STARTED) {
            throw FinishingWorkoutFailedException("Workout cannot be finished if it wasn't started")
        }
        apply(WorkoutFinishedEvent(id = this.id))
    }


    @EventSourcingHandler(payloadType = WorkoutFinishedEvent::class)
    fun onFinished() {
        this.status = FINISHED
    }

    fun getId() = id.copy()

}
