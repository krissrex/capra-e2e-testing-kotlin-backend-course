package no.liflig.bartenderservice

import kotlinx.serialization.json.Json
import no.liflig.bartenderservice.drinkmenu.AgeLimit
import no.liflig.bartenderservice.drinkmenu.Drink
import no.liflig.bartenderservice.orders.Customer
import no.liflig.bartenderservice.orders.DrinkOrder
import no.liflig.bartenderservice.orders.PaymentInfo

fun printExampleOrder() {
  val order =
      DrinkOrder(
          "1",
          Customer("1", AgeLimit.TWENTY),
          PaymentInfo("123-1234-123-456"),
          listOf(
              Drink("1", "Hansa", "98", "0.6", AgeLimit.EIGHTEEN),
              Drink("4", "Hakkespett", "120", "0.4", AgeLimit.TWENTY)))
  val json = Json.encodeToString(DrinkOrder.serializer(), order)
  println(json)
}
