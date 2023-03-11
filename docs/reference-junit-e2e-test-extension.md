# Reference: Junit End-to-end TestExtension

**Responsibility:** Wire up servers in `beforeAll` and teardown in `afterAll`. Clear data in `afterEach`. Inject
resources into test parameters.

This is a pattern, using
Junit [@ExtendWith](https://junit.org/junit5/docs/5.8.0/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/ExtendWith.html)
to delegate lifecycle callbacks and ParameterResolver to another class.
Using this pattern reduces clutter inside tests, and makes your test-setup reusable.

## General JUnit test execution order

Define a normal test class like this:

```kotlin
class MyThingTest {
  // Junit setup
  @BeforeAll
  fun beforeAll() {
  }

  @BeforeEach
  fun beforeEach() {
  }

  @AfterEach
  fun afterEach() {
  }

  @AfterAll
  fun afterAll() {
  }


  // Test cases
  @Test
  fun testOne() {
  }

  @Test
  fun testTwo() {
  }

  @Test
  fun testThree() {
  }
}
```

Then, the tests will be executed by Junit like this:

```text
beforeAll
    beforeEach
        testOne
    afterEach
    beforeEach
        testTwo
    afterEach
    beforeEach
        testThree
    afterEach
afterAll
```

## Usage

### Basic setup

```kotlin
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class EndToEndTestExtension :
  Extension,
  BeforeAllCallback,
  AfterEachCallback,
  BeforeEachCallback,
  AfterAllCallback,
  ParameterResolver {

  companion object {
    private var started = false

    /** For parameter resolution. */
    private val E2E_TEST_NAMESPACE = ExtensionContext.Namespace.create("E2E_TEST")
  }

  override fun beforeAll(context: ExtensionContext?) {
    if (!started) {
      started = true
      // TODO setupTestSuite()
    }
  }

  override fun beforeEach(context: ExtensionContext) {}

  override fun afterEach(context: ExtensionContext?) {}

  override fun afterAll(context: ExtensionContext?) {}

  override fun supportsParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Boolean {
    return false
  }

  override fun resolveParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Any? {
    return null
  }
}

```

### Parameter resolution

A test can add a parameter, and the extension will automatically inject a value for it:

```kotlin
@Test
fun `needs something from the extension`(jdbi: Jdbi, config: Config, testInputQueue: TestQueue) {
  // ...
}
```

We do this by storing instances statically in a `companion object`.
The `resolveParameter` will use Junit magic to get the instance from an "ExtensionContext Store",
or insert our instance if missing and return it.

````kotlin
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
 *
 *  - [Jdbi]
 */
class EndToEndExtension /*...*/ {
  companion object {
    private lateinit var jdbi: Jdbi
  }

  /** Must be ran in [beforeAll]. */
  private fun initJdbi() {
// TODO setup db
    jdbi = DatabaseConnection.jdbi
  }

  override fun supportsParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Boolean {
    return parameterContext.parameter.type in
      listOf(
        Jdbi::class.java,
// TODO add more
      )
  }

  override fun resolveParameter(
    parameterContext: ParameterContext,
    extensionContext: ExtensionContext,
  ): Any? {
    return when (parameterContext.parameter.type) {
      Jdbi::class.java -> parameters.jdbi(extensionContext)
// TODO add more
      else -> null
    }
  }

  private val parameters =
    object {
      fun jdbi(extensionContext: ExtensionContext): Jdbi =
        getObjectFromStore(extensionContext, jdbi)
    }

  /**
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

````

