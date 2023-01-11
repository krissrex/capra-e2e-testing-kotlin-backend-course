package no.liflig.baseline.support.observability

import org.http4k.core.Filter
import org.http4k.core.then
import org.http4k.filter.OpenTelemetryMetrics
import org.http4k.filter.OpenTelemetryTracing
import org.http4k.filter.ServerFilters

/**
 * Adds OpenTelemetry metrics, request counter and call tracing.
 *
 * You can inspect these values in CloudWatch or Xray with the appropriate
 * OpenTelemetry Collector set up as a sidecar container in CDK/ECS to this service.
 */
fun http4kOpenTelemetry(): Filter = ServerFilters.OpenTelemetryMetrics.RequestCounter()
  .then(ServerFilters.OpenTelemetryMetrics.RequestTimer())
  .then(ServerFilters.OpenTelemetryTracing())
