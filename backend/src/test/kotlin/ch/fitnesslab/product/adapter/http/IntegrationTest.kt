package ch.fitnesslab.product.adapter.http

import ch.fitnesslab.security.TestSecurityConfig
import ch.fitnesslab.utils.JsonLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Testcontainers
@Import(TestSecurityConfig::class)
abstract class IntegrationTest {
    @LocalServerPort
    protected var port: Int = 0

    protected val baseUrl = "http://localhost:$port"

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var jsonLoader: JsonLoader

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            val activeProfiles = System.getenv("SPRING_PROFILES_ACTIVE") ?: ""
            val isCiProfileActive = activeProfiles.contains("ci")
            val isDockerAvailable =
                try {
                    DockerClientFactory.instance().isDockerAvailable
                } catch (e: Exception) {
                    false
                }

            if (!isCiProfileActive && isDockerAvailable) {
                val postgres =
                    PostgreSQLContainer("postgres:16-alpine").apply {
                        withDatabaseName("fitnesslab")
                        withUsername("fitnesslab")
                        withPassword("fitnesslab")
                        withReuse(true)
                    }
                postgres.start()
                registry.add("spring.datasource.url", postgres::getJdbcUrl)
                registry.add("spring.datasource.username", postgres::getUsername)
                registry.add("spring.datasource.password", postgres::getPassword)
            }
            // If Docker is not available, it will use the datasource from application-test.yml
        }
    }
}
