package ch.fitnesslab.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!test")
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() } // Stateless API with JWT, CSRF not required
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers -> configureHeaders(headers) }
            .addFilterBefore(rateLimitingFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { authorize -> authorizeRoutes(authorize) }
            .oauth2ResourceServer { oauth2 -> configureJwtConverter(oauth2) }
        return http.build()
    }

    private fun configureHeaders(headers: HeadersConfigurer<HttpSecurity>) {
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
                ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
            }.frameOptions { it.deny() }
            .cacheControl { } // leave enabled for no-store on dynamic responses
            .xssProtection { } // no-op in modern browsers, safe to keep or remove
            .contentTypeOptions { } // adds X-Content-Type-Options: nosniff
    }

    private fun authorizeRoutes(authorize: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        authorize
            .requestMatchers(OPTIONS, "/**")
            .permitAll()
            .requestMatchers("/actuator/health", "/actuator/info")
            .permitAll()
            .requestMatchers(GET, "/api/version")
            .permitAll()
            // =======================
            //     PRODUCTS
            // =======================
            // Only ADMIN (i.e. only users having write:products) can create products
            .requestMatchers(POST, "/api/products/**")
            .hasAuthority("ROLE_write:products")
            // Admin + Trainer (i.e. users having read:products) can read products
            .requestMatchers(GET, "/api/products/**")
            .hasAuthority("ROLE_read:products")
            // Admin + Trainer (i.e. users having write:products OR read:products) can update products
            .requestMatchers(PUT, "/api/products/**")
            .hasAuthority("ROLE_write:products")
            // =======================
            //     CUSTOMERS
            // =======================
            // GET → read:customers
            .requestMatchers(GET, "/api/customers/**")
            .hasAuthority("ROLE_read:customers")
            // POST / PUT → write:customers
            .requestMatchers(POST, "/api/customers/**")
            .hasAuthority("ROLE_write:customers")
            .requestMatchers(PUT, "/api/customers/**")
            .hasAuthority("ROLE_write:customers")
            // =======================
            //     INVOICES
            // =======================
            // GET → read:invoices
            .requestMatchers(GET, "/api/invoices/**")
            .hasAuthority("ROLE_read:invoices")
            // POST → write:invoices
            .requestMatchers(POST, "/api/invoices/**")
            .hasAuthority("ROLE_write:invoices")
            // =======================
            //     CONTRACTS
            // =======================
            // GET → read:contracts
            .requestMatchers(GET, "/api/contracts/**")
            .hasAuthority("ROLE_read:contracts")
            // POST → write:contracts
            .requestMatchers(POST, "/api/contracts/**")
            .hasAuthority("ROLE_write:contracts")
            // =======================
            //     MEMBERSHIPS
            // =======================
            // sign-up → write:memberships
            .requestMatchers("/api/memberships/**")
            .hasAuthority("ROLE_write:memberships")
            // =======================
            //     DEFAULT
            // =======================
            .anyRequest()
            .authenticated()
    }

    private fun configureJwtConverter(oauth2: OAuth2ResourceServerConfigurer<HttpSecurity>) {
        oauth2.jwt { jwt ->
            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
        }
    }
}
