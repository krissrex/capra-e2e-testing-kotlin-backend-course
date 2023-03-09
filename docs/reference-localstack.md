# Reference: Localstack

## Usage

### Starting

See [testcontainers](./reference-testcontainers.md).

#### Creating an AWS SDK Client

First, set up a TestContainers container as `localStackContainer`.

SNS:

```kotlin
SnsClient.builder()
  .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.SNS))
  .credentialsProvider(
    StaticCredentialsProvider.create(
      AwsBasicCredentials.create(
        localStackContainer.getAccessKey(), localStackContainer.getSecretKey()
      )
    )
  )
  .region(Region.of(localStackContainer.getRegion()))
  .build()
```

### Internal API

#### SQS

https://docs.localstack.cloud/user-guide/aws/sqs/#developer-endpoints

```shell
curl "http://localhost:4566/_aws/sqs/messages?QueueUrl=http://queue.localhost.localstack.cloud:4566/000000000000/my-queue"
```

#### SNS

https://docs.localstack.cloud/user-guide/aws/sns/

To see SNS messages, you could create an SQS Queue, and subscribe it to the topic.
Or use an HTTP subscription to a webserver in your test-code (like Wiremock, or HTTP4k).
