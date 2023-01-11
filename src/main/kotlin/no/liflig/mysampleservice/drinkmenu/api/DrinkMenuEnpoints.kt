package no.liflig.mysampleservice.drinkmenu.api

import no.liflig.mysampleservice.Endpoint
import no.liflig.mysampleservice.drinkmenu.api.routes.GetDrinkMenuRoute

class DrinkMenuEnpoints : Endpoint {
  override val routes = listOf(
    GetDrinkMenuRoute("/menu").route,
  )
}
