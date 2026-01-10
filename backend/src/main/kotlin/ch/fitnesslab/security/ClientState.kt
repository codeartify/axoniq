package ch.fitnesslab.security

import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

data class ClientState(
    var windowStart: Instant,
    val requestCount: AtomicInteger = AtomicInteger(0),
    var violationCount: Int = 0,
    var lastViolation: Instant? = null,
)
