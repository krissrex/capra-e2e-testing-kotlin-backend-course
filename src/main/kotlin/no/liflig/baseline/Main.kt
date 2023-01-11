package no.liflig.baseline

import mu.KotlinLogging
import no.liflig.baseline.support.auth.DummyAuthService
import no.liflig.baseline.support.auth.ExamplePrincipal
import no.liflig.baseline.support.auth.ExamplePrincipalLog
import no.liflig.baseline.support.auth.toLog
import no.liflig.baseline.support.config.Config
import no.liflig.baseline.support.observability.OpenTelemetryConfig
import no.liflig.baseline.support.observability.http4kOpenTelemetry
import no.liflig.http4k.AuthService
import no.liflig.http4k.ServiceRouter
import no.liflig.http4k.health.HealthService
import no.liflig.http4k.health.createHealthService
import no.liflig.logging.RequestResponseLog
import no.liflig.logging.http4k.LoggingFilter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
  val config = Config.load()
  OpenTelemetryConfig().configure()
  logger.info { "BuildInfo: ${config.buildInfo}" }

  val healthService = createHealthService(config.applicationName, config.buildInfo)

  val authService: AuthService<ExamplePrincipal> = DummyAuthService
  val logHandler = LoggingFilter.createLogHandler(
    printStacktraceToConsole = true,
    principalLogSerializer = ExamplePrincipalLog.serializer(),
  )

  createApp(
    logHandler,
    config,
    authService,
    healthService,
  )
    .asServer(Jetty(config.serverPort))
    .start()
}

fun createApp(
  logHandler: (RequestResponseLog<ExamplePrincipalLog>) -> Unit,
  policy: Config,
  authService: AuthService<ExamplePrincipal>,
  healthService: HealthService,
) = ServiceRouter(
  logHandler,
  ExamplePrincipal::toLog,
  policy.corsPolicy.asPolicy(),
  authService,
  healthService,
) {
  logger.error(it) { "Error while retrieving principal" }
  Response(Status.UNAUTHORIZED)
}.routingHandler {
  additionalFilters += http4kOpenTelemetry()
  routes += api(policy, errorResponseRenderer)
}
