package com.codeartify.axoniq.adapter.presentation.http

import com.codeartify.axoniq.application.FinishWorkoutUseCase
import com.codeartify.axoniq.application.GetWorkoutByIdUseCase
import com.codeartify.axoniq.application.RecordSetUseCase
import com.codeartify.axoniq.application.StartWorkoutUseCase
import com.codeartify.axoniq.domain.values.*
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("/workouts")
class WorkoutController(
    private val startWorkoutUseCase: StartWorkoutUseCase,
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
    private val finishWorkoutUseCase: FinishWorkoutUseCase,
    private val recordSetUseCase: RecordSetUseCase
) {

    @PostMapping("/start")
    fun startWorkout(): CompletableFuture<WorkoutId?> {
        return startWorkoutUseCase.execute()
    }

    @GetMapping("/{id}")
    fun getWorkoutById(@PathVariable id: String) = "Hello $id"

    @PostMapping("/{id}/finish")
    fun finishWorkout(@PathVariable id: String): CompletableFuture<Void> {
        return finishWorkoutUseCase.execute(WorkoutId(id))
    }

    @PostMapping("/{id}/sets")
    fun recordSet(
        @PathVariable id: String,
        @RequestBody request: RecordSetRequest
    ): CompletableFuture<SetId?> {
        return recordSetUseCase.execute(
            workoutId = WorkoutId(id),
            repetitions = Repetitions(request.repetitions),
            weight = Weight(request.weight),
            exerciseName = ExerciseName(request.exerciseName)
        )
    }
}


