package ch.fitnesslab.security

import org.springframework.context.annotation.Bean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Bean
fun corsConfigurationSource(): CorsConfigurationSource {
    val configuration = CorsConfiguration()

    // Allow both local development and production origins
    configuration.allowedOrigins =
        listOf(
            "http://localhost:4200",
            "http://localhost:8081",
            "https://oliverzihler.ch",
            "https://www.oliverzihler.ch",
        )
    configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
    configuration.allowedHeaders = listOf("*")
    configuration.exposedHeaders = listOf("Location", "Authorization")
    configuration.allowCredentials = true
    configuration.maxAge = 3600

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
}
