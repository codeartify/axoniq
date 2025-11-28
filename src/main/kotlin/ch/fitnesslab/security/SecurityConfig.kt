package ch.fitnesslab.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    // Public endpoints (if any)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // All other endpoints require authentication
                    .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/product-contracts/**").hasAnyRole("ADMIN", "TRAINER")
                    .requestMatchers("/api/memberships/**").hasAnyRole("ADMIN", "TRAINER")
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:4200", "http://localhost:8081")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
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

            roles.filterIsInstance<String>()
                .map { org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_$it") }
        }
        return jwtAuthenticationConverter
    }
}
