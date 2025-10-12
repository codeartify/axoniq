package com.codeartify.axoniq.application

import com.codeartify.axoniq.domain.commands.FinishWorkoutCommand
import com.codeartify.axoniq.domain.values.WorkoutId
import com.codeartify.axoniq.infrastructure.UseCase
import org.axonframework.commandhandling.gateway.CommandGateway

@UseCase
class FinishWorkoutUseCase(
    private val commandGateway: CommandGateway
) {
    fun execute(workoutId: WorkoutId): java.util.concurrent.CompletableFuture<Void> {
        val command = FinishWorkoutCommand(id = workoutId)

        return commandGateway.send(command)
    }

}
