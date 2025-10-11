package com.codeartify.axoniq

import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController()
class WorkoutController(private val commandGateway: CommandGateway) {

    @PostMapping("workouts/start")
    fun startWorkout(): CompletableFuture<String> {
        return commandGateway.send(StartWorkoutCommand(id=WorkoutId.create()))
    }
}
