package no.liflig.mysampleservice.orders

import mu.KotlinLogging
import net.logstash.logback.marker.Markers
import software.amazon.awssdk.services.sqs.SqsClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SqsPoller(
  private val queueUrl: String,
  private val sqsClient: SqsClient,
  private val messageProcessor: OrderQueueProcessor,
) {

  private val log = KotlinLogging.logger { }
  private val executor = Executors.newSingleThreadScheduledExecutor()
  fun poll() {
    executor.scheduleAtFixedRate(
      {
        log.info { "Polling up to 20 seconds on queue $queueUrl..." }
        val response = sqsClient.receiveMessage { req ->
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
            ) { "Failed to process message ${message.messageId()}" }
          }
        }
      },
      0,
      20,
      TimeUnit.SECONDS,
    )
  }
}
