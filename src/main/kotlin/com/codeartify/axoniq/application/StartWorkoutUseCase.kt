package com.codeartify.axoniq.application

import com.codeartify.axoniq.domain.commands.StartWorkoutCommand
import com.codeartify.axoniq.domain.values.WorkoutId
import com.yourpackage.common.UseCase
import org.axonframework.commandhandling.gateway.CommandGateway
import java.util.concurrent.CompletableFuture

@UseCase
class StartWorkoutUseCase(private val commandGateway: CommandGateway) {

    fun execute(): CompletableFuture<WorkoutId?> {
        val command = StartWorkoutCommand(id = WorkoutId.create())

        return commandGateway.send<WorkoutId>(command)
    }
}
