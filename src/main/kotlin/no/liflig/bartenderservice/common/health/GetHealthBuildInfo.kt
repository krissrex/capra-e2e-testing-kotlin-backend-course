@file:UseSerializers(InstantSerializer::class)

package no.liflig.bartenderservice.common.health

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.liflig.bartenderservice.common.serialization.InstantSerializer
import no.liflig.properties.intRequired
import no.liflig.properties.stringNotNull
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Properties

/**
 * Create [HealthBuildInfo] based on build.properties injected by the build.
 */
fun Properties.getBuildInfo() = BuildInfo(
  timestamp = try {
    Instant.parse(stringNotNull("build.timestamp"))
  } catch (ex: DateTimeParseException) {
    Instant.ofEpochMilli(
      0L,
    )
  },
  commit = stringNotNull("build.commit"),
  branch = stringNotNull("build.branch"),
  number = try {
    intRequired("build.number")
  } catch (ex: IllegalArgumentException) {
    0
  },
)

@Serializable
data class BuildInfo(
  val timestamp: Instant,
  val commit: String,
  val branch: String,
  val number: Int,
)
