package no.liflig.bartenderservice.common.config

import java.io.File
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.int
import org.http4k.lens.string

data class Config(
    val applicationName: String,
    val serverPort: Int,
    val buildInfo: BuildInfo,
    var database: DbConfig,
    val queuePollerEnabled: Boolean,
    var awsConfig: AwsConfig,
    val paymentProviderUrl: String
) {

  companion object {
    private fun env(): Environment =
        Environment.ENV overrides
            Environment.JVM_PROPERTIES overrides
            Environment.fromFileIfExist(File("overrides.properties")) overrides
            Environment.fromResource("application.properties")

    fun load() =
        env().let { env ->
          Config(
              appName(env),
              port(env),
              BuildInfo.create(env),
              DbConfig.create(env),
              queuePollerEnabled(env),
              AwsConfig.create(env),
              paymentProviderUrl(env))
        }
  }
}

private val appName = EnvironmentKey.string().required("service.name")
private val port = EnvironmentKey.int().defaulted("server.port", 8080)
private val queuePollerEnabled = EnvironmentKey.boolean().defaulted("orderQueue.enabled", true)
private val paymentProviderUrl = EnvironmentKey.string().required("paymentProvider.url")

private fun Environment.Companion.fromFileIfExist(file: File): Environment {
  return if (file.exists() && file.isFile) {
    Environment.from(file)
  } else {
    Environment.EMPTY
  }
}
