package no.liflig.bartenderservice

import no.liflig.bartenderservice.common.config.AwsConfig
import no.liflig.bartenderservice.common.config.Config
import no.liflig.bartenderservice.common.config.DbConfig
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import test.util.QueueWithDlq
import test.util.TopicWithQueue
import test.util.createOrderProcessingEventTopic
import test.util.createOrdersQueue

/**
 * ```kt
 *  @ExtendWith(EndToEndTestExtension::class)
 *  class KotlinE2eTest {}
 *  ```
 *
 * ## Injecting application values into test parameters
 *
 * This extension is a [ParameterResolver], which means your test methods can specify parameters
 * with certain types, and this extension will inject them:
 * ```kt
 * @IntegrationTest
 * void testAppShouldWork(Config config, Jdbi jdbi) { ...; }
 * }
 * ```
 *
 * The following items can be injected:
 * - [Config]
 */
class EndToEndTestExtension :
    Extension,
    BeforeAllCallback,
    AfterEachCallback,
    BeforeEachCallback,
    AfterAllCallback,
    ParameterResolver {

  companion object {
    private var started = false

    private lateinit var config: Config
    private lateinit var postgres: PostgreSQLContainer<*>
    private lateinit var localstack: LocalStackContainer

    private lateinit var sqsClient: SqsClient
    private lateinit var snsClient: SnsClient

    private lateinit var ordersQueue: QueueWithDlq
    private lateinit var orderEventsTopic: TopicWithQueue

    /** For parameter resolution. */
    private val E2E_TEST_NAMESPACE = ExtensionContext.Namespace.create("E2E_TEST")
  }

  override fun beforeAll(context: ExtensionContext?) {
    if (!started) {
      started = true
      setupTestSuite()
    }
  }

  fun setupTestSuite() {
    config = Config.load().copy(queuePollerEnabled = true)
    initDatabase(config)
    initAws(config)
  }

  private fun initAws(config: Config) {
    val localstackImage = DockerImageName.parse("localstack/localstack:1.4.0")
    localstack =
        LocalStackContainer(localstackImage)
            .withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS)
    localstack.start()

    sqsClient = test.util.createSqsClient(localstack)
    snsClient = test.util.createSnsClient(localstack)

    ordersQueue = createOrdersQueue(sqsClient)
    orderEventsTopic = createOrderProcessingEventTopic(snsClient, sqsClient)

    config.awsConfig =
        AwsConfig(
            awsUseLocalstack = true,
            snsRegion = Region.US_EAST_1,
            sqsRegion = Region.US_EAST_1,
            snsEndpointOverride =
                localstack.getEndpointOverride(LocalStackContainer.Service.SNS).toString(),
            sqsEndpointOverride =
                localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString(),
            orderNotificationTopicArn = orderEventsTopic.topicArn,
            orderQueueUrl = ordersQueue.queueUrl)
  }

  private fun initDatabase(config: Config) {
    postgres = PostgreSQLContainer("postgres:14.2-alpine")
    postgres.withDatabaseName("app").withUsername("test").withPassword("test").start()

    config.database =
        DbConfig(
            username = "test",
            password = "test",
            dbname = "app",
            port = postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            hostname = "localhost",
            jdbcUrl = postgres.getJdbcUrl())
  }

  override fun beforeEach(context: ExtensionContext) {}

  override fun afterEach(context: ExtensionContext?) {}

  override fun afterAll(context: ExtensionContext?) {
    postgres.stop()
    localstack.stop()
  }

  override fun supportsParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
  ): Boolean {
    return parameterContext.parameter.type in
        listOf(
            Config::class.java,
            SqsClient::class.java,
            SnsClient::class.java,
            QueueWithDlq::class.java,
            TopicWithQueue::class.java
            // TODO add more
            )
  }

  override fun resolveParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
  ): Any? {
    return when (parameterContext.parameter.type) {
      Config::class.java -> parameters.config(extensionContext)
      SnsClient::class.java -> parameters.snsClient(extensionContext)
      SqsClient::class.java -> parameters.sqsClient(extensionContext)
      QueueWithDlq::class.java -> parameters.ordersQueue(extensionContext)
      TopicWithQueue::class.java -> parameters.orderEventsTopic(extensionContext)
      // TODO add more
      else -> null
    }
  }

  private val parameters =
      object {
        /** Send a copy, for the config only, because test may mutate it. */
        fun config(extensionContext: ExtensionContext): Config =
            getObjectFromStore(extensionContext, config).copy()

        fun sqsClient(extensionContext: ExtensionContext): SqsClient =
            getObjectFromStore(extensionContext, sqsClient)

        fun snsClient(extensionContext: ExtensionContext): SnsClient =
            getObjectFromStore(extensionContext, snsClient)

        fun ordersQueue(extensionContext: ExtensionContext): QueueWithDlq =
            getObjectFromStore(extensionContext, ordersQueue)

        fun orderEventsTopic(extensionContext: ExtensionContext): TopicWithQueue =
            getObjectFromStore(extensionContext, orderEventsTopic)
        // TODO add more
      }

  /**
   * Ignore this method.
   *
   * Avoid using members directly when injecting into test methods because of potential problems
   * when running test in parallel. Instead, we are using the cross-test JUnit ExtensionContext
   * member store. Ref: https://stackoverflow.com/a/58586208/258510
   */
  private fun <T> getObjectFromStore(extensionContext: ExtensionContext, obj: T & Any): T {
    val returnedObject: T? =
        extensionContext.root.getStore(E2E_TEST_NAMESPACE).get(obj, obj::class.java) as T

    return if (returnedObject == null) {
      extensionContext.root.getStore(E2E_TEST_NAMESPACE).put(obj.javaClass.canonicalName, obj)
      obj
    } else {
      returnedObject
    }
  }
}
