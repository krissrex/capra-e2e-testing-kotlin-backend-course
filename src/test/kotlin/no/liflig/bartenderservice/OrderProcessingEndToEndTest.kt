package no.liflig.bartenderservice

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import java.util.concurrent.TimeUnit
import mu.KotlinLogging
import no.liflig.bartenderservice.common.config.Config
import no.liflig.snapshot.verifyJsonSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.shaded.org.awaitility.Awaitility
import software.amazon.awssdk.services.sqs.SqsClient
import test.util.Integration
import test.util.QueueWithDlq
import test.util.TopicWithQueue
import test.util.loadTestFile

@ExtendWith(EndToEndTestExtension::class)
class OrderProcessingEndToEndTest {

  @Integration
  @Test
  fun `should process an order from sqs, persist the order to database, and produce two progress events on sns`(
      config: Config,
      sqsClient: SqsClient,
      ordersQueue: QueueWithDlq,
      orderEventsTopic: TopicWithQueue
  ) {
    // Given
    val myExternalApi: WireMockServer =
        WireMockServer(WireMockConfiguration.wireMockConfig().port(6789))
    myExternalApi.start()

    myExternalApi.stubFor(
        WireMock.post("/api/payments")
            .willReturn(WireMock.status(200).withBody("paid successfully")))

    App(config.copy(paymentProviderUrl = myExternalApi.baseUrl() + "/api/payments")).start()

    val queueUrl = ordersQueue.queueUrl

    // When
    TestFiles.T1.send(sqsClient, queueUrl)

    Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
      val availableMessages = orderEventsTopic.messageCount(sqsClient)
      assertThat(availableMessages).isEqualTo(2)
    }

    val messages = orderEventsTopic.getMessages(sqsClient)
    assertThat(messages.map { it.body() })
        .containsAll(
            listOf(
                """{"orderId":"1","at":"2023-03-15T20:06:26.645754Z"}""",
                """{"orderId":"1","at":"2023-03-15T20:06:29.693910Z"}"""))

    myExternalApi.verify(
        WireMock.postRequestedFor(WireMock.urlPathEqualTo("/api/payments"))
            .withRequestBody(
                WireMock.equalToJson("""{ "cardNumber": "123-1234-123-456", "price": "218"}""")))

    /*    TestFiles.T2.send(sqsClient, queueUrl)
    TestFiles.T3.send(sqsClient, queueUrl)
    TestFiles.T4.send(sqsClient, queueUrl)
    TestFiles.T5.send(sqsClient, queueUrl)
    TestFiles.T6.send(sqsClient, queueUrl)
    TestFiles.T7.send(sqsClient, queueUrl)*/

    // Then
    // TODO await a message out from SNS
    // TODO await another message out from SNS
    // TODO check for order in database

    assertThat("y").isEqualTo("y")

    verifyJsonSnapshot("example/sns-message.json", """{"message": "hello world"}""")

    Thread.sleep(10000)
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
