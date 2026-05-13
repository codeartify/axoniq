package ch.fitnesslab.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

@Configuration
class JwtDecoderConfig {
    @Bean
    fun jwtDecoder(
        @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") jwkSetUri: String,
        @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") issuerUri: String,
    ): NimbusJwtDecoder {
        val decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .validateType(false)
            .build()

        decoder.setJwtValidator(
            JwtValidators.createAtJwtValidator()
                .issuer(issuerUri)
                .audience("https://www.oliverzihler.ch")
                .clientId("k0o6L1mxBtIkHC6as89XkbEK2VXnFEWk")
                .build(),
        )

        return decoder
    }
}
