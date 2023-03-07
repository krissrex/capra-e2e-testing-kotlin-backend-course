package no.liflig.bartenderservice.drinkmenu.api.routes

import no.liflig.bartenderservice.drinkmenu.domain.AgeLimit
import no.liflig.bartenderservice.drinkmenu.domain.Drink
import no.liflig.bartenderservice.drinkmenu.domain.DrinkMenu
import org.http4k.contract.ContractRoute
import org.http4k.contract.RouteMetaDsl
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization

private val menuBody = KotlinxSerialization.autoBody<DrinkMenu>().toLens()

fun getDrinkMenuRoute(): ContractRoute {
  fun openApiSpec(): RouteMetaDsl.() -> Unit = {
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

  val route =
      "/menu" meta
          openApiSpec() bindContract
          Method.GET to
          { req ->
            Response(Status.OK).with(menuBody of DrinkMenu.createDefaultMenu())
          }

  return route
}
