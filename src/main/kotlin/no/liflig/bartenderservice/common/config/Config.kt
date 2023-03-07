package no.liflig.bartenderservice.common.config

import java.io.File
import no.liflig.bartenderservice.common.health.BuildInfo
import no.liflig.bartenderservice.common.health.getBuildInfo
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.int
import org.http4k.lens.string

data class Config(
    val applicationName: String,
    val serverPort: Int,
    val buildInfo: BuildInfo,
    val database: DbConfig,
    val queuePollerEnabled: Boolean,
    val awsConfig: AwsConfig
) {

  companion object {
    private fun env(): Environment =
        Environment.ENV overrides
            Environment.JVM_PROPERTIES overrides
            Environment.from(File("overrides.properties")) overrides
            Environment.fromResource("application.properties")

    fun load() =
        env().let { env ->
          Config(
              appName(env),
              port(env),
              getBuildInfo(env),
              DbConfig.create(env),
              queuePollerEnabled(env),
              AwsConfig.create(env),
          )
        }
  }
}

private val appName = EnvironmentKey.string().required("service.name")
private val port = EnvironmentKey.int().defaulted("server.port", 8080)
private val queuePollerEnabled = EnvironmentKey.boolean().defaulted("orderQueue.enabled", true)
