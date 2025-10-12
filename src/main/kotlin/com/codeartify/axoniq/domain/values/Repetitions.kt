package com.codeartify.axoniq.domain.values

import com.codeartify.axoniq.domain.exception.RepetitionInvalidException

@JvmInline
value class Repetitions(val count: Int) {
    init {
        if (count <= 0) {
            throw RepetitionInvalidException("Repetitions count must be > 0, but was $count")
        }
    }
}
