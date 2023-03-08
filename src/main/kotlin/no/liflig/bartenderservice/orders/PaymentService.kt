package no.liflig.bartenderservice.orders

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import no.liflig.bartenderservice.common.serialization.PrettyHttp4kKotlinxSerialization.auto
import org.http4k.client.JavaHttpClient
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with

class PaymentService(
    private val paymentProviderUrl: String,
    private val httpClient: HttpHandler = JavaHttpClient()
) {

  /** @return null if ok, or a deviation on failure */
  fun collectPayment(cardNumber: String, totalPrice: BigDecimal): PaymentDeviation? {
    require(totalPrice > BigDecimal.ZERO) { "Price cannot be 0 or negative: $totalPrice" }

    val requestBody = PaymentBody(cardNumber = cardNumber, price = totalPrice.toPlainString())
    val request = Request(Method.POST, paymentProviderUrl).with(paymentBody of requestBody)

    val response =
        try {
          httpClient(request)
        } catch (ex: Exception) {
          return PaymentDeviation.PaymentServiceUnavailable
        }

    if (!response.status.successful) {
      return PaymentDeviation.PaymentServiceUnavailable
    }

    return when (response.bodyString()) {
      "paid successfully" -> null
      "insufficient balance" -> PaymentDeviation.NotEnoughBalanceOnCustomerCard
      "invalid card number" -> PaymentDeviation.InvalidCardNumber
      else -> throw RuntimeException("Unrecognized payment provider response")
    }
  }
}

@Serializable internal data class PaymentBody(val cardNumber: String, val price: String)

private val paymentBody = Body.auto<PaymentBody>().toLens()

sealed interface PaymentDeviation {
  object PaymentServiceUnavailable : PaymentDeviation
  object NotEnoughBalanceOnCustomerCard : PaymentDeviation
  object InvalidCardNumber : PaymentDeviation
}
