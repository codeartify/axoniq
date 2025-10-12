package com.codeartify.axoniq.adapters.presentation.http

import com.codeartify.axoniq.application.GetWorkoutByIdUseCase
import com.codeartify.axoniq.application.StartWorkoutUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/workouts")
class WorkoutController(
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase

) {

    @PostMapping("/start")
    fun startWorkout(): CompletableFuture<WorkoutStartedResponse> {
        return startWorkoutUseCase.execute()
            .thenApply {
                WorkoutStartedResponse(workoutId = it?.value)
            }
    }

    @GetMapping("/{id}")
    fun getWorkoutById(@PathVariable id: String) = "Hello $id"

}
