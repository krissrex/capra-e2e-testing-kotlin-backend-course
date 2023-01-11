package no.liflig.baseline.support.config

import no.liflig.baseline.support.health.getHealthBuildInfo
import no.liflig.properties.intRequired
import no.liflig.properties.loadProperties
import no.liflig.properties.string
import no.liflig.properties.stringNotEmpty
import org.http4k.core.Credentials
import java.util.Properties

class Config private constructor(val properties: Properties) {

  // Change these
  val applicationName = "liflig-rest-baseline"

  val corsPolicy = CorsConfig.from(properties)
  val serverPort = properties.intRequired("server.port")
  val buildInfo = properties.getHealthBuildInfo()
  val openapiCredentials = Credentials(
    user = properties.stringNotEmpty("api.openapi.credentials.user"),
    password = properties.string("api.openapi.credentials.password") ?: "",
  )

  val database = DbConfig.create(properties)

  companion object {
    fun load() = Config(loadProperties())
  }
}
