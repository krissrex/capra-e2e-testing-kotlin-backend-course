package no.liflig.mysampleservice.drinkmenu.api.routes

import no.liflig.mysampleservice.Route
import no.liflig.mysampleservice.drinkmenu.domain.AgeLimit
import no.liflig.mysampleservice.drinkmenu.domain.Drink
import no.liflig.mysampleservice.drinkmenu.domain.DrinkMenu
import org.http4k.contract.RouteMetaDsl
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization

class GetDrinkMenuRoute(basePath: String) : Route {

  private val menuBody = KotlinxSerialization.autoBody<DrinkMenu>().toLens()

  override val route = basePath meta meta() bindContract Method.GET to { req ->
    Response(Status.OK).with(menuBody of DrinkMenu.createDefaultMenu())
  }

  private fun meta(): RouteMetaDsl.() -> Unit = {
    summary = "Get the drink menu"
    operationId = "getDrinkMenu"
    description = "Get drink menu with all the available drinks"
    tags += Tag("drink")

    returning(
      Status.OK,
      menuBody to DrinkMenu(listOf(Drink("1", "Eksempel-pils", "69", "0.5", AgeLimit.EIGHTEEN))),
      description = "The menu with available drinks",
    )
  }
}
