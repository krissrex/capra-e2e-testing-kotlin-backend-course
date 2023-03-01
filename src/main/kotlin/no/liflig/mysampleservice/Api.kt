package no.liflig.mysampleservice

import mu.KotlinLogging
import no.liflig.mysampleservice.drinkmenu.api.routes.getDrinkMenuRoute
import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

private val log = KotlinLogging.logger {}

private val requestLogging = Filter { next: HttpHandler ->
  { req: Request ->
    log.debug { "${req.method} ${req.uri}" }
    next(req)
  }
}

fun api(): RoutingHttpHandler {
  return "api" / "v1" bind contract {
    routes += getDrinkMenuRoute()
  }.withFilter(requestLogging)
}
