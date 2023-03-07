package no.liflig.bartenderservice.common.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.string
import software.amazon.awssdk.regions.Region

data class AwsConfig(
    val orderQueueUrl: String,
    val orderNotificationTopicArn: String,
    val awsUseLocalstack: Boolean,
    val snsEndpointOverride: String?,
    val sqsEndpointOverride: String?,
    val sqsRegion: Region = Region.US_EAST_1,
    val snsRegion: Region = Region.US_EAST_1,
) {
  companion object {
    fun create(env: Environment): AwsConfig =
        AwsConfig(
            orderQueueUrl = orderQueueUrl(env),
            orderNotificationTopicArn = orderNotificationTopicArn(env),
            awsUseLocalstack = awsUseLocalstack(env),
            sqsEndpointOverride = sqsEndpointOverride(env),
            snsEndpointOverride = snsEndpointOverride(env),
        )
  }
}

private val orderQueueUrl = EnvironmentKey.string().required("orderQueue.sqs.url")
private val orderNotificationTopicArn =
    EnvironmentKey.string().required("orderProcessingNotification.sns.arn")
private val awsUseLocalstack = EnvironmentKey.boolean().defaulted("aws.localstack.enabled", false)
private val sqsEndpointOverride =
    EnvironmentKey.string().optional("aws.localstack.sqs.endpointOverride")
private val snsEndpointOverride =
    EnvironmentKey.string().optional("aws.localstack.sns.endpointOverride")
