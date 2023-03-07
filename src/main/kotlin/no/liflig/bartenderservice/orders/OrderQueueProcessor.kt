package no.liflig.bartenderservice.orders

import mu.KotlinLogging
import no.liflig.bartenderservice.drinkmenu.domain.AgeLimit
import org.slf4j.MDC
import kotlin.time.Duration.Companion.seconds

/**
 * Picks incoming orders from the work queue and tries to process them.
 */
class OrderQueueProcessor(
  private val ageLimitPolicy: AgeLimitPolicy,
  private val paymentService: PaymentService,
  private val orderRepository: OrderRepository,
  private val orderReadyNotifyer: OrderReadyNotifyer,
) {
  private val log = KotlinLogging.logger { }

  fun process(queueMessage: String) {
    val order = DrinkOrder.fromJson(queueMessage)
    MDC.putCloseable("order.id", order.orderId).use {
      log.info { "Received order $order" }

      ageLimitPolicy.enforceAgeLimitPolicy(order)
      log.info { "Age limit enforced" }

      val paymentDeviation = paymentService.collectPayment(order.paymentInfo.cardNumber, order.totalPrice)
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

      Thread.sleep(3.seconds.inWholeMilliseconds)

      orderReadyNotifyer.notifyOrderReady(order.orderId)
    }
  }
}

class AgeLimitPolicy {
  fun enforceAgeLimitPolicy(order: DrinkOrder) {
    when (order.customer.age) {
      AgeLimit.NONE -> require(order.orderLines.none { it.ageLimit != AgeLimit.NONE }) { "Customer is too young!" }
      AgeLimit.EIGHTEEN -> require(order.orderLines.none { it.ageLimit == AgeLimit.TWENTY })
      AgeLimit.TWENTY -> Unit // Can buy everything.
    }
  }
}
