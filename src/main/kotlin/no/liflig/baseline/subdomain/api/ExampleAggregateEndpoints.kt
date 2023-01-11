package no.liflig.baseline.subdomain.api

import no.liflig.baseline.Endpoint
import no.liflig.baseline.subdomain.api.routes.GetExampleAggregateRoute

class ExampleAggregateEndpoints : Endpoint {
  companion object {
    const val basePath = "/example"
  }
  // TODO

  override val routes = listOf(
    GetExampleAggregateRoute(basePath).route,
  )
}
