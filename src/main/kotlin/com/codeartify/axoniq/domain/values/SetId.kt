package com.codeartify.axoniq.domain.values

import java.util.UUID

data class SetId(val value: String) {
    companion object {
        fun create() = SetId(UUID.randomUUID().toString())
    }
}
