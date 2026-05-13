package ch.fitnesslab.security

import org.springframework.context.annotation.Bean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter

@Bean
fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
    val jwtAuthenticationConverter = JwtAuthenticationConverter()
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
        val permissions = jwt.claims["permissions"] as? List<*> ?: emptyList<String>()
        val scopes = (jwt.claims["scope"] as? String)
            ?.split(" ")
            .orEmpty()

        (permissions + scopes)
            .filterIsInstance<String>()
            .distinct()
            .map { permission ->
                SimpleGrantedAuthority("ROLE_$permission")
            }
    }
    return jwtAuthenticationConverter
}
