@file:UseSerializers(InstantSerializer::class)

package no.liflig.baseline.support.health

import kotlinx.serialization.UseSerializers
import no.liflig.baseline.support.serialization.InstantSerializer
import no.liflig.http4k.health.HealthBuildInfo
import no.liflig.properties.intRequired
import no.liflig.properties.stringNotNull
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Properties

/**
 * Create [HealthBuildInfo] based on build.properties injected by the build.
 */
fun Properties.getHealthBuildInfo() = HealthBuildInfo(
  timestamp = try { Instant.parse(stringNotNull("build.timestamp")) } catch (ex: DateTimeParseException) {
    Instant.ofEpochMilli(
      0L,
    )
  },
  commit = stringNotNull("build.commit"),
  branch = stringNotNull("build.branch"),
  number = try { intRequired("build.number") } catch (ex: IllegalArgumentException) { 0 },
)
