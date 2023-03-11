package no.liflig.bartenderservice

import mu.KotlinLogging
import no.liflig.snapshot.verifyJsonSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient
import test.util.Integration
import test.util.loadTestFile

class OrderProcessingEndToEndTest {

  @Integration
  @Test
  fun `should process an order from sqs, persist the order to database, and produce two progress events on sns`() {
    // Given
    // TODO start the app

    val sqsClient = TODO()
    val queueUrl = TODO()
    // When
    // TODO send a message to the queue

    TestFiles.T1.send(sqsClient, queueUrl)
    TestFiles.T2.send(sqsClient, queueUrl)
    TestFiles.T3.send(sqsClient, queueUrl)
    TestFiles.T4.send(sqsClient, queueUrl)
    TestFiles.T5.send(sqsClient, queueUrl)
    TestFiles.T6.send(sqsClient, queueUrl)
    TestFiles.T7.send(sqsClient, queueUrl)

    // Then
    // TODO await a message out from SNS
    // TODO await another message out from SNS
    // TODO check for order in database

    assertThat("y").isEqualTo("y")

    verifyJsonSnapshot("example/sns-message.json", """{"message": "hello world"}""")

    TODO("Not yet implemented")
  }
}

private enum class TestFiles(val path: String) {
  T1("/sample-orders/1_customer-aged20-buys-beer-limited18-and-spirits-limited20.json"),
  T2("/sample-orders/2_customer-aged18-buys-beer-limited18-and-spirits-limited20.json"),
  T3("/sample-orders/3_customer-aged18-buys-beer-limited18.json"),
  T4("/sample-orders/4_customer-aged18-buys-nothing.json"),
  T5("/sample-orders/5_customer-underage-buys-beer-limited18.json"),
  T6("/sample-orders/6_customer-underage-buys-booze-limited20.json"),
  T7("/sample-orders/7_customer-underage-buys-soda-nolimit.json");

  companion object {
    private val log = KotlinLogging.logger {}
  }

  fun send(sqsClient: SqsClient, queueUrl: String) {
    val body = loadTestFile(this.path)
    check(body.isNotEmpty()) { "Failed to load $body" }

    val response =
        sqsClient.sendMessage { req ->
          req.queueUrl(queueUrl)
          req.messageBody(body)
        }
    log.info { "Sent ${this.name} as ${response.messageId()}" }
  }
}
