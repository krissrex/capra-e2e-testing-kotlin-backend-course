package no.liflig.bartenderservice.common.health

import no.liflig.bartenderservice.common.config.BuildInfo
import no.liflig.bartenderservice.common.serialization.PrettyHttp4kKotlinxSerialization.auto
import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

val requestBody = Body.auto<BuildInfo>().toLens()

fun healthRoute(buildInfo: BuildInfo): RoutingHttpHandler {
  return "health" bind { _: Request -> Response(Status.OK).with(requestBody of buildInfo) }
}
