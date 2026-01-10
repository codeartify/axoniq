package ch.fitnesslab.security

import org.springframework.context.annotation.Bean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter

@Bean
fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
    val jwtAuthenticationConverter = JwtAuthenticationConverter()
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
        val roles = realmAccess?.get("roles") as? List<*> ?: emptyList<String>()

        roles
            .filterIsInstance<String>()
            .map { role ->
                SimpleGrantedAuthority("ROLE_$role")
            }
    }
    return jwtAuthenticationConverter
}
