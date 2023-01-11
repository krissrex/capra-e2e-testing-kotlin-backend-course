package no.liflig.mysampleservice

import mu.KotlinLogging
import no.liflig.http4k.AuthService
import no.liflig.http4k.health.createHealthService
import no.liflig.logging.http4k.LoggingFilter
import no.liflig.mysampleservice.common.auth.DummyAuthService
import no.liflig.mysampleservice.common.auth.ExamplePrincipal
import no.liflig.mysampleservice.common.auth.ExamplePrincipalLog
import no.liflig.mysampleservice.common.config.Config
import org.http4k.server.Jetty
import org.http4k.server.asServer

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
  val config = Config.load()
  logger.info { "BuildInfo: ${config.buildInfo}" }

  val healthService = createHealthService(config.applicationName, config.buildInfo)

  val authService: AuthService<ExamplePrincipal> = DummyAuthService
  val logHandler = LoggingFilter.createLogHandler(
    printStacktraceToConsole = true,
    principalLogSerializer = ExamplePrincipalLog.serializer(),
  )

  createApi(
    logHandler,
    config,
    authService,
    healthService,
  ).asServer(Jetty(config.serverPort))
    .start()
}
