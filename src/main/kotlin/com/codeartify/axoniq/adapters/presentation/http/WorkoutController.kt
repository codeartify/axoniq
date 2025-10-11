package com.codeartify.axoniq.adapters.presentation.http

import com.codeartify.axoniq.domain.StartWorkoutCommand
import com.codeartify.axoniq.domain.WorkoutId
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/workouts")
class WorkoutController(private val commandGateway: CommandGateway) {

    @PostMapping("/start")
    fun startWorkout(): CompletableFuture<WorkoutStartedResponse> {
        return commandGateway.send<WorkoutId>(StartWorkoutCommand(id = WorkoutId.create()))
            .thenApply {
                WorkoutStartedResponse(workoutId = it.value)
            }
    }
}
