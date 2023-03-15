package test.util

import java.util.UUID
import kotlinx.serialization.json.JsonPrimitive
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.QueueAttributeName

fun createOrdersQueue(sqsClient: SqsClient): QueueWithDlq {
  return QueueWithDlq.create(sqsClient, "orders")
}

fun createOrderProcessingEventTopic(snsClient: SnsClient, sqsClient: SqsClient): TopicWithQueue {
  return TopicWithQueue.create(sqsClient, snsClient, "order-events")
}

fun messageCount(sqsClient: SqsClient, queueUrl: String): Int {
  val response =
      sqsClient.getQueueAttributes { req ->
        req.queueUrl(queueUrl)
        req.attributeNames(
            QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
            QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE,
            QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_DELAYED)
      }

  val totalMessageCount =
      response
          .attributes()
          .filterKeys {
            it in
                listOf(
                    QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES,
                    QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_NOT_VISIBLE,
                    QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES_DELAYED)
          }
          .mapNotNull { it.value.toIntOrNull() }
          .sum()

  return totalMessageCount
}

data class TopicWithQueue(val topicArn: String, val queueUrl: String) {

  fun messageCount(sqsClient: SqsClient): Int = messageCount(sqsClient, queueUrl)

  /** Gets at most 10 messages after at most 20 seconds of waiting. */
  fun getMessages(sqsClient: SqsClient): List<Message> {
    val messageResponse =
        sqsClient.receiveMessage { req ->
          req.queueUrl(queueUrl)
          req.maxNumberOfMessages(10)
          req.waitTimeSeconds(20)
        }

    // Delete them to successfully receive them. Otherwise, you get them back later.
    val receivedMessages =
        messageResponse.messages().map {
          DeleteMessageBatchRequestEntry.builder()
              .receiptHandle(it.receiptHandle())
              .id(UUID.randomUUID().toString())
              .build()
        }
    sqsClient.deleteMessageBatch { req ->
      req.queueUrl(queueUrl)
      req.entries(receivedMessages)
    }

    return messageResponse.messages()
  }
  companion object {
    fun create(sqsClient: SqsClient, snsClient: SnsClient, topicName: String): TopicWithQueue {
      val topicResponse = snsClient.createTopic { req -> req.name(topicName) }
      val topicArn = topicResponse.topicArn()

      // To see the output from the topic, we subscribe a queue to it.
      // Localstack does not have an internal endpoint to inspect messages.
      val eventCollectorQueue =
          sqsClient.createQueue { req -> req.queueName("$topicName-collector") }
      val queueArnResponse =
          sqsClient.getQueueAttributes { req ->
            req.queueUrl(eventCollectorQueue.queueUrl())
            req.attributeNames(QueueAttributeName.QUEUE_ARN)
          }
      val queueArn = queueArnResponse.attributes().get(QueueAttributeName.QUEUE_ARN)

      snsClient.subscribe { req ->
        req.topicArn(topicArn)
        req.protocol("sqs").endpoint(queueArn)
        req.attributes(mapOf("RawMessageDelivery" to "true"))
      }

      return TopicWithQueue(topicArn = topicArn, queueUrl = eventCollectorQueue.queueUrl())
    }
  }
}

data class QueueWithDlq(val queueUrl: String, val dlqUrl: String) {

  fun messageCount(sqsClient: SqsClient): Int = messageCount(sqsClient, queueUrl)
  fun dlqMessageCount(sqsClient: SqsClient): Int = messageCount(sqsClient, dlqUrl)

  companion object {
    /** Create two SQS Queues, and attach one as a Dead-Letter Queue to the main queue. */
    fun create(sqsClient: SqsClient, queueName: String): QueueWithDlq {
      // The Dead-Letter Queue (DLQ) contains failed messages
      val dlqResponse = sqsClient.createQueue { req -> req.queueName("$queueName-dlq") }
      val dlqArnResponse =
          sqsClient.getQueueAttributes { req ->
            req.queueUrl(dlqResponse.queueUrl())
            req.attributeNames(QueueAttributeName.QUEUE_ARN)
          }
      val dlqArn = dlqArnResponse.attributes().get(QueueAttributeName.QUEUE_ARN)

      // Create the actual queue
      val queueResponse =
          sqsClient.createQueue { req ->
            req.queueName(queueName)
            // Attach the DLQ to this queue.
            req.attributes(
                mapOf(
                    QueueAttributeName.REDRIVE_POLICY to
                        """{"deadLetterTargetArn": ${JsonPrimitive(dlqArn)}, "maxReceiveCount": "2"}""",
                    QueueAttributeName.VISIBILITY_TIMEOUT to "4"))
          }

      return QueueWithDlq(queueUrl = queueResponse.queueUrl(), dlqUrl = dlqResponse.queueUrl())
    }
  }
}
