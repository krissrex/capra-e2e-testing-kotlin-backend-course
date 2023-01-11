package no.liflig.baseline.support.observability

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import mu.KotlinLogging
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Call the [OpenTelemetryConfig.configure] method.
 *
 * See [opentelemetry.io/docs/instrumentation/java/manual_instrumentation](https://opentelemetry.io/docs/instrumentation/java/manual_instrumentation/)
 * for usage of the SDK.
 */
class OpenTelemetryConfig {
  private val log = KotlinLogging.logger { }

  fun configure() = try {
    log.info { "Configuring OpenTelemetry" }

    // OTel uses JUL at debug level
    Logger.getLogger("io.opentelemetry").level = Level.WARNING
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // The java agent will configure the GlobalOpenTelemetry, so no opentelemetry-sdk is needed here.

    log.info { "Configuration of OpenTelemetry complete" }
  } catch (e: Throwable) {
    log.error(e) { "Failed to configure OpenTelemetry" }
  }

  companion object {
    const val instrumentationName = "opentelemetry-instrumentation-" + "<service-name>"
    const val instrumentationVersion = "1.0.0"

    val meter: Meter by lazy {
      GlobalOpenTelemetry.get()
        .meterBuilder(instrumentationName)
        .setInstrumentationVersion(instrumentationVersion)
        .build()
    }

    val tracer: Tracer by lazy {
      GlobalOpenTelemetry.get()
        .tracerBuilder(instrumentationName)
        .setInstrumentationVersion(instrumentationVersion)
        .build()
    }
  }
}
