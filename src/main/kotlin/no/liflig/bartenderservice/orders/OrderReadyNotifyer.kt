@file:UseSerializers(InstantSerializer::class)

package no.liflig.bartenderservice.orders

import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import no.liflig.bartenderservice.common.serialization.InstantSerializer

private val log = KotlinLogging.logger {}

class OrderReadyNotifyer(
    private val snsSender: SnsSender,
) {
  fun notifyProcessingStarted(orderId: String) {
    log.info { "Sending started event $orderId" }
    snsSender.send(
        ProcessingStartedEvent(
                orderId = orderId,
            )
            .toJson(),
    )
  }

  fun notifyOrderReady(orderId: String) {
    log.info { "Sending ready event $orderId" }
    snsSender.send(
        ProcessingCompletedEvent(
                orderId = orderId,
            )
            .toJson(),
    )
  }
}

@Serializable
data class ProcessingStartedEvent(
    val orderId: String,
    val at: Instant = Instant.now(),
    val type: String = "STARTED",
) {
  fun toJson(): String = Json.encodeToString(serializer(), this)
}

@Serializable
data class ProcessingCompletedEvent(
    val orderId: String,
    val at: Instant = Instant.now(),
    val type: String = "COMPLETE",
) {
  fun toJson(): String = Json.encodeToString(serializer(), this)
}
