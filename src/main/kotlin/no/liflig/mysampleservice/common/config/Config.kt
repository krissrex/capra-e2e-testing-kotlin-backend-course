package no.liflig.mysampleservice.common.config

import no.liflig.mysampleservice.common.health.getBuildInfo
import no.liflig.properties.boolean
import no.liflig.properties.intRequired
import no.liflig.properties.loadProperties
import no.liflig.properties.stringNotNull
import java.util.Properties

class Config private constructor(val properties: Properties) {

  val applicationName = properties.stringNotNull("service.name")

  val serverPort = properties.intRequired("server.port")
  val buildInfo = properties.getBuildInfo()

  val database = DbConfig.create(properties)

  val queuePollerEnabled = properties.boolean("orderQueue.enabled") ?: true
  val awsConfig = AwsConfig.create(properties)

  companion object {
    fun load() = Config(loadProperties())
  }
}
