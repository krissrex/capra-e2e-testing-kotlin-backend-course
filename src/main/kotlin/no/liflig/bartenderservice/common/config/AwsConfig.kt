package no.liflig.bartenderservice.common.config

import java.util.Properties
import no.liflig.properties.boolean
import no.liflig.properties.string
import no.liflig.properties.stringNotEmpty
import software.amazon.awssdk.regions.Region

data class AwsConfig(
    val orderQueueUrl: String,
    val orderNotificationTopicArn: String,
    val awsUseLocalstack: Boolean,
    val snsEndpointOverride: String?,
    val sqsEndpointOverride: String?,
    val sqsRegion: Region = Region.EU_WEST_1,
    val snsRegion: Region = Region.EU_WEST_1,
) {
  companion object {
    fun create(properties: Properties): AwsConfig =
        AwsConfig(
            orderQueueUrl = properties.stringNotEmpty("orderQueue.sqs.url"),
            orderNotificationTopicArn =
                properties.stringNotEmpty("orderProcessingNotification.sns.arn"),
            awsUseLocalstack = properties.boolean("aws.localstack.enabled") ?: false,
            sqsEndpointOverride = properties.string("aws.localstack.sqs.endpointOverride"),
            snsEndpointOverride = properties.string("aws.localstack.sns.endpointOverride"),
        )
  }
}
