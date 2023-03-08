package no.liflig.bartenderservice

import java.net.URI
import mu.KotlinLogging
import no.liflig.bartenderservice.common.config.Config
import no.liflig.bartenderservice.common.database.DatabaseConnection
import no.liflig.bartenderservice.orders.AgeLimitPolicy
import no.liflig.bartenderservice.orders.AwsSnsSender
import no.liflig.bartenderservice.orders.OrderQueueProcessor
import no.liflig.bartenderservice.orders.OrderReadyNotifyer
import no.liflig.bartenderservice.orders.OrderRepository
import no.liflig.bartenderservice.orders.PaymentService
import no.liflig.bartenderservice.orders.SqsPoller
import org.http4k.server.Jetty
import org.http4k.server.asServer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient

/** Responsible for wiring up all dependencies and starting services. */
class App(private val config: Config) {
  private val logger = KotlinLogging.logger {}

  fun start() {
    logger.info { "${config.applicationName} running build ${config.buildInfo.toJson()}" }

    logger.debug { "Config: $config" }

    startRestApi(config)
    if (config.queuePollerEnabled) {
      startOrderQueuePoller(config)
    }
  }

  private fun startRestApi(config: Config) {
    logger.info { "Starting API" }
    api(config).asServer(Jetty(config.serverPort)).start()
    logger.info { "Started API on port ${config.serverPort}" }
  }

  private fun startOrderQueuePoller(config: Config) {
    logger.info { "Starting queue poller" }

    val databaseConnection = DatabaseConnection(config.database)
    databaseConnection.initialize()

    SqsPoller(
            config.awsConfig.orderQueueUrl,
            createSqsClient(config),
            OrderQueueProcessor(
                ageLimitPolicy = AgeLimitPolicy(),
                paymentService = PaymentService(paymentProviderUrl = config.paymentProviderUrl),
                orderRepository = OrderRepository(databaseConnection.jdbi),
                orderReadyNotifyer =
                    OrderReadyNotifyer(
                        AwsSnsSender(
                            config.awsConfig.orderNotificationTopicArn,
                            createSnsClient(config),
                        ),
                    ),
            ),
        )
        .start()
    logger.info {
      "Started queue poller on queue ${config.awsConfig.orderQueueUrl} and" +
          " sending events to ${config.awsConfig.orderNotificationTopicArn}"
    }
  }
}

private fun createSnsClient(config: Config): SnsClient =
    SnsClient.builder()
        .region(config.awsConfig.snsRegion)
        .apply {
          if (config.awsConfig.awsUseLocalstack) {
            this.endpointOverride(URI(config.awsConfig.snsEndpointOverride!!))
            this.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("x", "x"),
                ),
            )
          }
        }
        .build()

private fun createSqsClient(config: Config): SqsClient =
    SqsClient.builder()
        .region(config.awsConfig.sqsRegion)
        .apply {
          if (config.awsConfig.awsUseLocalstack) {
            this.endpointOverride(URI(config.awsConfig.sqsEndpointOverride!!))
            this.credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("x", "x"),
                ),
            )
          }
        }
        .build()
