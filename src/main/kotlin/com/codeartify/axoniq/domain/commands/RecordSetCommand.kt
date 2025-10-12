package com.codeartify.axoniq.domain.commands

import com.codeartify.axoniq.domain.WorkoutId
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class RecordSetCommand(@TargetAggregateIdentifier val workoutId: WorkoutId)
