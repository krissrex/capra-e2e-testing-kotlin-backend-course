@file:UseSerializers(InstantSerializer::class)

package no.liflig.bartenderservice.orders

import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import no.liflig.bartenderservice.common.serialization.InstantSerializer

class OrderReadyNotifyer(
    private val snsSender: SnsSender,
) {
  fun notifyProcessingStarted(orderId: String) {
    snsSender.send(
        ProcessingStartedEvent(
                orderId = orderId,
            )
            .toJson(),
    )
  }

  fun notifyOrderReady(orderId: String) {
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
