package ch.fitnesslab.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Very simple in‑memory rate limiter with exponential backoff per client IP.
 *
 * - Allows up to MAX_REQUESTS_PER_WINDOW per WINDOW_DURATION for each IP.
 * - After the limit is exceeded, the client receives 429 Too Many Requests.
 * - A Retry-After header is sent with backoff delay that grows exponentially
 *   with the number of consecutive violations, capped at MAX_BACKOFF.
 *
 * This is *not* a replacement for a proper rate limiting gateway or WAF,
 * but it helps mitigate naive abuse / DoS from a single client.
 */
@Bean
fun rateLimitingFilter(): OncePerRequestFilter =
    object : OncePerRequestFilter() {
        private val WINDOW_DURATION: Duration = Duration.ofSeconds(10)
        private val MAX_REQUESTS_PER_WINDOW = 50 // tune to your needs

        private val INITIAL_BACKOFF: Duration = Duration.ofSeconds(5)
        private val MAX_BACKOFF: Duration = Duration.ofMinutes(5)

        private val clients: MutableMap<String, ClientState> = ConcurrentHashMap()

        override fun shouldNotFilter(request: HttpServletRequest): Boolean {
            // Optionally limit rate limiting to specific paths:
            // return !request.requestURI.startsWith("/api/")
            return !request.requestURI.startsWith("/api/")
        }

        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain,
        ) {
            val clientId = clientKey(request)
            val now = Instant.now()

            val state =
                clients.compute(clientId) { _, existing ->
                    val s = existing ?: ClientState(windowStart = now)
                    // Reset window if expired
                    if (Duration.between(s.windowStart, now) > WINDOW_DURATION) {
                        s.windowStart = now
                        s.requestCount.set(0)
                    }
                    s
                }!!

            val currentCount = state.requestCount.incrementAndGet()
            if (currentCount > MAX_REQUESTS_PER_WINDOW) {
                // Violation – increase backoff, up to cap
                state.violationCount += 1
                state.lastViolation = now

                val backoffSeconds = calculateBackoffSeconds(state.violationCount)
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.setHeader("Retry-After", backoffSeconds.toString())
                response.contentType = "application/json"
                response.writer.write(
                    """
                    {
                      "error": "too_many_requests",
                      "message": "Rate limit exceeded. Retry after $backoffSeconds seconds."
                    }
                    """.trimIndent(),
                )
                return
            }

            filterChain.doFilter(request, response)
        }

        private fun calculateBackoffSeconds(violationCount: Int): Long {
            // Exponential backoff: 5s, 10s, 20s, 40s, ...
            val rawBackoff = INITIAL_BACKOFF.seconds * (1L shl (violationCount - 1).coerceAtLeast(0))
            return rawBackoff.coerceAtMost(MAX_BACKOFF.seconds)
        }

        private fun clientKey(request: HttpServletRequest): String {
            // Basic heuristic: use X-Forwarded-For if present, otherwise remoteAddr
            val forwardedFor = request.getHeader("X-Forwarded-For")
            return forwardedFor?.split(",")?.firstOrNull()?.trim()
                ?: request.remoteAddr
        }
    }
