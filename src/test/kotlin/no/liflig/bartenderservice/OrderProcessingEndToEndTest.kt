package no.liflig.bartenderservice

import no.liflig.snapshot.verifyJsonSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.util.Integration
import test.util.loadTestFile

class OrderProcessingEndToEndTest {

  @Integration
  @Test
  fun `should process an order from sqs, persist the order to database, and produce two progress events on sns`() {
    // Given
    // TODO start the app

    // When
    // TODO send a message to the queue

    loadTestFile("/sample-orders/1_customer-aged20-buys-beer-limited18-and-spirits-limited20.json")
    loadTestFile("/sample-orders/2_customer-aged18-buys-beer-limited18-and-spirits-limited20.json")
    loadTestFile("/sample-orders/3_customer-aged18-buys-beer-limited18.json")
    loadTestFile("/sample-orders/4_customer-aged18-buys-nothing.json")
    loadTestFile("/sample-orders/5_customer-underage-buys-beer-limited18.json")
    loadTestFile("/sample-orders/6_customer-underage-buys-booze-limited20.json")
    loadTestFile("/sample-orders/7_customer-underage-buys-soda-nolimit.json")

    // Then
    // TODO await a message out from SNS
    // TODO await another message out from SNS
    // TODO check for order in database

    assertThat("y").isEqualTo("y")

    verifyJsonSnapshot("example/sns-message.json", """{"message": "hello world"}""")

    TODO("Not yet implemented")
  }
}
