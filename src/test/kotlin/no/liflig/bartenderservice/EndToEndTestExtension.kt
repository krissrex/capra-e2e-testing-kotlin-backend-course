package no.liflig.bartenderservice

import no.liflig.bartenderservice.common.config.Config
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

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
    config = Config.load()
    // initDatabase(config)
  }

  override fun beforeEach(context: ExtensionContext) {}

  override fun afterEach(context: ExtensionContext?) {}

  override fun afterAll(context: ExtensionContext?) {}

  override fun supportsParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
  ): Boolean {
    return parameterContext.parameter.type in
        listOf(
            Config::class.java
            // TODO add more
            )
  }

  override fun resolveParameter(
      parameterContext: ParameterContext,
      extensionContext: ExtensionContext,
  ): Any? {
    return when (parameterContext.parameter.type) {
      Config::class.java -> parameters.config(extensionContext)
      // TODO add more
      else -> null
    }
  }

  private val parameters =
      object {
        /** Send a copy, for the config only, because test may mutate it. */
        fun config(extensionContext: ExtensionContext): Config =
            getObjectFromStore(extensionContext, config).copy()

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
