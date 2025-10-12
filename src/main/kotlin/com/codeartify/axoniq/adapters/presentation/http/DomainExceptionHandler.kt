package com.codeartify.axoniq.adapters.presentation.http

import com.codeartify.axoniq.domain.exception.FinishingWorkoutFailedException
import com.codeartify.axoniq.domain.exception.RecordingSetFailedException
import com.codeartify.axoniq.domain.exception.RepetitionInvalidException
import com.codeartify.axoniq.domain.exception.WeightInvalidException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DomainExceptionHandler {
    data class ApiError(val message: String)

    @ExceptionHandler(
        value = [
            FinishingWorkoutFailedException::class,
            RecordingSetFailedException::class,
            RepetitionInvalidException::class,
            WeightInvalidException::class
        ]
    )
    fun handleDomainExceptions(ex: RuntimeException): ResponseEntity<ApiError> {
        val msg = ex.message ?: "Bad request"
        return ResponseEntity.status(BAD_REQUEST)
            .body(ApiError(message = msg))
    }

}
