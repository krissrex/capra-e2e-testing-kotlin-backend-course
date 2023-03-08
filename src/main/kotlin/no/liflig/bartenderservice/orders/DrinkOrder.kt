package no.liflig.bartenderservice.orders

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import no.liflig.bartenderservice.drinkmenu.AgeLimit
import no.liflig.bartenderservice.drinkmenu.Drink

/** A list of drinks ordered by a customer. */
@Serializable
data class DrinkOrder(
    val orderId: String,
    val customer: Customer,
    val paymentInfo: PaymentInfo,
    val orderLines: List<Drink>,
) {
  @Transient val totalPrice: BigDecimal = orderLines.sumOf { BigDecimal(it.price) }

  companion object {
    fun fromJson(json: String): DrinkOrder = Json.decodeFromString(serializer(), json)
  }
}

@Serializable
data class Customer(
    val id: String,
    val age: AgeLimit,
)

@Serializable
data class PaymentInfo(
    val cardNumber: String,
)
