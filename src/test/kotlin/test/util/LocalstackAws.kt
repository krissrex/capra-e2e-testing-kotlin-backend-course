package test.util

import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient

fun createSqsClient(localstackContainer: LocalStackContainer): SqsClient {
  return SqsClient.builder()
      .endpointOverride(localstackContainer.getEndpointOverride(LocalStackContainer.Service.SQS))
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
