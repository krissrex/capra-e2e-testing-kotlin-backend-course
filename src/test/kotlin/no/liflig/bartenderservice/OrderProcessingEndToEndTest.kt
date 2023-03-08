package no.liflig.bartenderservice

import no.liflig.snapshot.verifyJsonSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.util.Integration

class OrderProcessingEndToEndTest {

  @Integration
  @Test
  fun `should process an order from sqs, persist the order to database, and produce two progress events on sns`() {
    // Given
    // TODO start the app

    // When
    // TODO send a message to the queue

    // Then
    // TODO await a message out from SNS
    // TODO await another message out from SNS
    // TODO check for order in database

    assertThat("y").isEqualTo("y")

    verifyJsonSnapshot("example/sns-message.json", """{"message": "hello world"}""")

    TODO("Not yet implemented")
  }
}
