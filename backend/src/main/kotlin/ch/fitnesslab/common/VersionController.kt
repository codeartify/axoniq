package ch.fitnesslab.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class VersionInfo(
    val version: String,
    val buildTime: String,
    val commit: String?
)

@RestController
@RequestMapping("/api/version")
class VersionController(
    @Value("\${app.version:0.0.1-SNAPSHOT}") private val version: String,
    @Value("\${app.build-time:unknown}") private val buildTime: String,
    @Value("\${app.commit:unknown}") private val commit: String
) {
    @GetMapping
    fun getVersion(): VersionInfo {
        return VersionInfo(
            version = version,
            buildTime = buildTime,
            commit = commit
        )
    }
}
