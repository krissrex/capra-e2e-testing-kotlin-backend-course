package no.liflig.mysampleservice.orders

class OrderRepository {
  fun createOrder(order: DrinkOrder): OrderCreationDeviation {
    TODO("Not yet implemented")
    return OrderCreationDeviation.DatabaseUnavailable
  }
}

sealed interface OrderCreationDeviation {
  object DatabaseUnavailable : OrderCreationDeviation
  data class OrderAlreadyExists(val orderId: String) : OrderCreationDeviation
}
