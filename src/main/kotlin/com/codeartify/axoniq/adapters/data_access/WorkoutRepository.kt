package com.codeartify.axoniq.adapters.data_access

import com.codeartify.axoniq.domain.Workout
import com.codeartify.axoniq.domain.values.WorkoutId
import org.springframework.data.jpa.repository.JpaRepository

interface WorkoutRepository : JpaRepository<Workout, WorkoutId> {

}
