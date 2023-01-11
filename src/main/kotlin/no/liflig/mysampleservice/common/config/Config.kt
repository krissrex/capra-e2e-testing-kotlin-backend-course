package no.liflig.mysampleservice.common.config

import no.liflig.mysampleservice.common.health.getHealthBuildInfo
import no.liflig.properties.boolean
import no.liflig.properties.intRequired
import no.liflig.properties.loadProperties
import no.liflig.properties.string
import no.liflig.properties.stringNotEmpty
import no.liflig.properties.stringNotNull
import org.http4k.core.Credentials
import java.util.Properties

class Config private constructor(val properties: Properties) {

  val applicationName = properties.stringNotNull("service.name")

  val corsPolicy = CorsConfig.from(properties)
  val serverPort = properties.intRequired("server.port")
  val buildInfo = properties.getHealthBuildInfo()
  val openapiCredentials = Credentials(
    user = properties.stringNotEmpty("api.openapi.credentials.user"),
    password = properties.string("api.openapi.credentials.password") ?: "",
  )

  val database = DbConfig.create(properties)

  val queuePollerEnabled = properties.boolean("orderQueue.enabled") ?: true
  val awsConfig = AwsConfig.create(properties)

  companion object {
    fun load() = Config(loadProperties())
  }
}
