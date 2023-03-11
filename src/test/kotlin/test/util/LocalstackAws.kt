package test.util

import kotlinx.serialization.json.JsonPrimitive
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.QueueAttributeName

fun createSqsClient(localstackContainer: LocalStackContainer): SqsClient {
  return SqsClient.builder()
      .endpointOverride(localstackContainer.getEndpointOverride(LocalStackContainer.Service.SNS))
      .credentialsProvider(
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create(
                  localstackContainer.accessKey, localstackContainer.secretKey)))
      .region(Region.of(localstackContainer.region))
      .build()
}

fun createSnsClient(localstackContainer: LocalStackContainer): SnsClient {
  return SnsClient.builder()
      .endpointOverride(localstackContainer.getEndpointOverride(LocalStackContainer.Service.SNS))
      .credentialsProvider(
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create(
                  localstackContainer.accessKey, localstackContainer.secretKey)))
      .region(Region.of(localstackContainer.region))
      .build()
}

fun createOrdersQueue(sqsClient: SqsClient): QueueWithDlq {
  return QueueWithDlq.create(sqsClient, "orders")
}

fun createOrderProcessingEventTopic(snsClient: SnsClient, sqsClient: SqsClient): TopicWithQueue {
  return TopicWithQueue.create(sqsClient, snsClient, "order-events")
}

data class TopicWithQueue(val topicArn: String, val queueUrl: String) {
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
                        """{"deadLetterTargetArn": ${JsonPrimitive(dlqArn)}, "maxReceiveCount": "2"}"""))
          }

      return QueueWithDlq(queueUrl = queueResponse.queueUrl(), dlqUrl = dlqResponse.queueUrl())
    }
  }
}
