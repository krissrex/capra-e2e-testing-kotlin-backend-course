package no.liflig.bartenderservice.orders

import java.util.UUID
import mu.KotlinLogging
import org.jdbi.v3.core.Jdbi

private val log = KotlinLogging.logger {}

class OrderRepository(private val jdbi: Jdbi) {

  /** @return null if success, or a deviation on failure. */
  fun createOrder(order: DrinkOrder): OrderCreationDeviation? {
    try {
      val uuid = UUID.randomUUID().toString()
      jdbi.inTransaction<Unit, Exception> { handle ->
        handle
            .createUpdate("INSERT INTO orders (id, data) VALUES (:id, :data::jsonb)")
            .bind("id", uuid)
            .bind("data", order.toJson())
            .execute()
      }
      log.info { "Created order $uuid" }

      return null
    } catch (e: Exception) {
      return OrderCreationDeviation.DatabaseUnavailable
    }
  }
}

sealed interface OrderCreationDeviation {
  object DatabaseUnavailable : OrderCreationDeviation
  data class OrderAlreadyExists(val orderId: String) : OrderCreationDeviation
}
