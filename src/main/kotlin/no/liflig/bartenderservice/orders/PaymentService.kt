package no.liflig.bartenderservice.orders

import java.math.BigDecimal

class PaymentService {
  fun collectPayment(cardNumber: String, totalPrice: BigDecimal): PaymentDeviation {
    require(totalPrice > BigDecimal.ZERO) { "Price cannot be 0 or negative: $totalPrice" }

    TODO("Not yet implemented")
    return PaymentDeviation.PaymentServiceUnavailable
  }
}

sealed interface PaymentDeviation {
  object PaymentServiceUnavailable : PaymentDeviation
  object NotEnoughBalanceOnCustomerCard : PaymentDeviation
  object InvalidCardNumber : PaymentDeviation
}
