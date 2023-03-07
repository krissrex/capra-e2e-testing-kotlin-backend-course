package no.liflig.bartenderservice.orders

import kotlin.concurrent.thread
import mu.KotlinLogging
import net.logstash.logback.marker.Markers
import software.amazon.awssdk.services.sqs.SqsClient

/** Responsible for polling messages from a queue and forwarding them to [messageProcessor]. */
class SqsPoller(
    private val queueUrl: String,
    private val sqsClient: SqsClient,
    private val messageProcessor: OrderQueueProcessor,
) {

  private val log = KotlinLogging.logger {}

  /** Loops forever untill the JVM exits. */
  private val pollingThread: Thread =
      thread(start = false) {
        while (true) {
          try {
            poll()
          } catch (ex: Throwable) {
            log.error(ex) { "Failure when polling. Continuing regardless..." }
          }
        }
      }

  fun start() {
    check(!pollingThread.isAlive) { "Polling already started" }

    pollingThread.start()
  }

  private fun poll() {
    log.info { "Polling up to 20 seconds on queue $queueUrl..." }
    val response =
        sqsClient.receiveMessage { req ->
          req.queueUrl(queueUrl)
          req.maxNumberOfMessages(10)
          req.waitTimeSeconds(20)
          req.visibilityTimeout(30)
        }

    response.messages().forEach { message ->
      try {
        messageProcessor.process(message.body())
        sqsClient.deleteMessage { req ->
          req.queueUrl(queueUrl)
          req.receiptHandle(message.receiptHandle())
        }
      } catch (ex: Throwable) {
        log.error(
            Markers.append("sqsMessage", message.body()),
            ex,
        ) {
          "Failed to process message ${message.messageId()}"
        }
      }
    }
  }
}
