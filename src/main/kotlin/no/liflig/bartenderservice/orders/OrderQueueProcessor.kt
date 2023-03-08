package no.liflig.bartenderservice.orders

import kotlin.jvm.Throws
import kotlin.time.Duration.Companion.seconds
import mu.KotlinLogging
import no.liflig.bartenderservice.drinkmenu.AgeLimit
import org.slf4j.MDC

/** Picks incoming orders from the work queue and tries to process them. */
class OrderQueueProcessor(
    private val ageLimitPolicy: AgeLimitPolicy,
    private val paymentService: PaymentService,
    private val orderRepository: OrderRepository,
    private val orderReadyNotifyer: OrderReadyNotifyer,
) {
  private val log = KotlinLogging.logger {}

  fun process(queueMessage: String) {
    val order = DrinkOrder.fromJson(queueMessage)
    MDC.putCloseable("order.id", order.orderId).use {
      log.info { "Received order $order" }

      try {
        ageLimitPolicy.enforceAgeLimitPolicy(order)
        log.info { "Age limit enforced" }
      } catch (e: IllegalArgumentException) {
        log.warn { "Invalid age when purchasing this order. Discarding order" }
        return
      }

      val paymentDeviation =
          paymentService.collectPayment(order.paymentInfo.cardNumber, order.totalPrice)
      if (paymentDeviation != null) {
        log.warn { "Failed to collect payment: $paymentDeviation" }
        throw RuntimeException("Failed to collect payment: $paymentDeviation")
      }

      val orderCreationDeviation = orderRepository.createOrder(order)
      if (orderCreationDeviation != null) {
        log.warn { "Failed to create order in database: $orderCreationDeviation" }
        throw RuntimeException("Failed to create order in database: $orderCreationDeviation")
      }

      orderReadyNotifyer.notifyProcessingStarted(order.orderId)

      // Let the bar-tender do some work...
      Thread.sleep(3.seconds.inWholeMilliseconds)

      orderReadyNotifyer.notifyOrderReady(order.orderId)
      log.info { "Processing of order ${order.orderId} complete." }
    }
  }
}

class AgeLimitPolicy {

  /**
   * @throws IllegalArgumentException if the customer is not allowed to purchase an item in their
   *   order
   */
  @Throws(IllegalArgumentException::class)
  fun enforceAgeLimitPolicy(order: DrinkOrder) {
    when (order.customer.age) {
      AgeLimit.NONE ->
          require(order.orderLines.none { it.ageLimit != AgeLimit.NONE }) {
            "Customer is too young!"
          }
      AgeLimit.EIGHTEEN -> require(order.orderLines.none { it.ageLimit == AgeLimit.TWENTY })
      AgeLimit.TWENTY -> Unit // Can buy everything.
    }
  }
}
