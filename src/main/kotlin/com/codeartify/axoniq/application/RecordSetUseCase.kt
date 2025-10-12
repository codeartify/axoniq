package com.codeartify.axoniq.application

import com.codeartify.axoniq.domain.commands.RecordSetCommand
import com.codeartify.axoniq.domain.values.*
import com.codeartify.axoniq.infrastructure.UseCase
import org.axonframework.commandhandling.gateway.CommandGateway
import java.util.concurrent.CompletableFuture

@UseCase
class RecordSetUseCase(
    private val commandGateway: CommandGateway
) {

    fun execute(
        workoutId: WorkoutId, repetitions: Repetitions, weight: Weight, exerciseName: ExerciseName
    ): CompletableFuture<SetId?> {
        val setId = SetId.create()

        val command = RecordSetCommand(
            workoutId = workoutId,
            exerciseName = exerciseName,
            setId = setId,
            repetitions = repetitions,
            weight = weight
        )

        return commandGateway.send<SetId>(command)
    }
}
