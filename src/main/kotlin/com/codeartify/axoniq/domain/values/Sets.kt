package com.codeartify.axoniq.domain.values

import com.codeartify.axoniq.domain.Set

class Sets {

    private val sets = mutableListOf<Set>()

    fun add(set: Set) = sets.add(set)
    fun get() = sets.toList()
}
