package no.liflig.bartenderservice

import mu.KotlinLogging
import no.liflig.bartenderservice.common.config.Config
import no.liflig.bartenderservice.common.health.healthRoute
import no.liflig.bartenderservice.drinkmenu.getDrinkMenuRoute
import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

private val log = KotlinLogging.logger {}

private val requestFilters =
    ServerFilters.CatchAll { error ->
          log.error(error) { "Response failed" }
          Response(Status.INTERNAL_SERVER_ERROR)
        }
        .then(ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive))
        .then(DebuggingFilters.PrintRequestAndResponse())

fun api(config: Config): RoutingHttpHandler {
  return requestFilters.then(
      routes(
          healthRoute(config.buildInfo),
          "api" / "v1" bind contract { routes += getDrinkMenuRoute() }))
}
