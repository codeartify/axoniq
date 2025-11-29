package ch.fitnesslab.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() } // Stateless API with JWT, CSRF not required
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // Modern security headers
            .headers { headers ->
                headers
                    .httpStrictTransportSecurity { hsts ->
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000) // 1 year, tune for prod
                    }.contentSecurityPolicy { csp ->
                        // Very strict default; relax as needed for your frontend
                        csp.policyDirectives(
                            "default-src 'self'; " +
                                "script-src 'self'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data:; " +
                                "font-src 'self' data:; " +
                                "frame-ancestors 'none'; " +
                                "object-src 'none'; " +
                                "base-uri 'self'",
                        )
                    }.referrerPolicy { ref ->
                        ref.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                    }.frameOptions { it.deny() }
                    .cacheControl { } // leave enabled for no-store on dynamic responses
                    .xssProtection { } // no-op in modern browsers, safe to keep or remove
                    .contentTypeOptions { } // adds X-Content-Type-Options: nosniff
            }.addFilterBefore(rateLimitingFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers("/api/customers/**")
                    .hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/products/**")
                    .hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/invoices/**")
                    .hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/product-contracts/**")
                    .hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/memberships/**")
                    .hasAnyRole("ADMIN", "TRAINER")
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // In production, restrict to your real frontend origins only
        configuration.allowedOrigins =
            listOf(
                "http://localhost:4200",
                "http://localhost:8081",
            )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.exposedHeaders = listOf("Location")
        configuration.allowCredentials = true
        configuration.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
            val roles = realmAccess?.get("roles") as? List<*> ?: emptyList<String>()

            roles
                .filterIsInstance<String>()
                .map { role ->
                    org.springframework.security.core.authority
                        .SimpleGrantedAuthority("ROLE_$role")
                }
        }
        return jwtAuthenticationConverter
    }

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
}
