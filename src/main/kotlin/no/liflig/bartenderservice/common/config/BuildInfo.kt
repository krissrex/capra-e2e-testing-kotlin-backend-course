@file:UseSerializers(InstantSerializer::class)

package no.liflig.bartenderservice.common.config

import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import no.liflig.bartenderservice.common.serialization.InstantSerializer
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.instant
import org.http4k.lens.int
import org.http4k.lens.string

private val json = Json { prettyPrint = true }

@Serializable
data class BuildInfo(
    val timestamp: Instant,
    val commit: String,
    val branch: String,
    val number: Int,
) {
  fun toJson(): String {
    return json.encodeToString(serializer(), this)
  }

  companion object {
    /** Create [HealthBuildInfo] based on build.properties injected by the build. */
    fun create(env: Environment) =
        BuildInfo(
            timestamp = timestamp(env),
            commit = commit(env),
            branch = branch(env),
            number = number(env),
        )
  }
}

private val timestamp =
    EnvironmentKey.instant().defaulted("build.timestamp", Instant.ofEpochMilli(0))
private val commit = EnvironmentKey.string().required("build.commit")
private val branch = EnvironmentKey.string().required("build.branch")
private val number = EnvironmentKey.int().defaulted("build.number", 0)
