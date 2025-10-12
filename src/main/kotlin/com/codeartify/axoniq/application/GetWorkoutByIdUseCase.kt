package com.codeartify.axoniq.application

import com.codeartify.axoniq.infrastructure.UseCase
import org.axonframework.queryhandling.QueryGateway

@UseCase
class GetWorkoutByIdUseCase(private val queryGateway: QueryGateway) {

    //fun execute(id: WorkoutId) = queryGateway.query<Workout>()
}
