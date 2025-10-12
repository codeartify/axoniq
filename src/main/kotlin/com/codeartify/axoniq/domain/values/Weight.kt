package com.codeartify.axoniq.domain.values

import com.codeartify.axoniq.domain.exception.WeightInvalidException

@JvmInline
value class Weight(val value: Double) {
    init {
        if (value <= 0) {
            throw WeightInvalidException("Weight must be > 0, but was $value")
        }
    }
}
