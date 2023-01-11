package no.liflig.mysampleservice.subdomain.api

import no.liflig.mysampleservice.Endpoint
import no.liflig.mysampleservice.subdomain.api.routes.GetExampleAggregateRoute

class ExampleAggregateEndpoints : Endpoint {
  companion object {
    const val basePath = "/example"
  }
  // TODO

  override val routes = listOf(
    GetExampleAggregateRoute(basePath).route,
  )
}
