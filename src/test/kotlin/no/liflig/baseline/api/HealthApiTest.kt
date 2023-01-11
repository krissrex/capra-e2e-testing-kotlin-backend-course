package no.liflig.baseline.api

import no.liflig.baseline.createApp
import no.liflig.baseline.support.auth.DummyAuthService
import no.liflig.baseline.support.config.Config
import no.liflig.http4k.health.HealthBuildInfo
import no.liflig.http4k.health.HealthService
import org.http4k.core.Method
import org.http4k.core.Status
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.Test
import java.time.Instant

class HealthApiTest {

  @Test
  internal fun `health should respond 200 ok`() {
    // Given
    val router = createApp(
      logHandler = { },
      policy = Config.load(),
      authService = DummyAuthService,
      healthService = HealthService(
        name = "Test-app",
        buildInfo = HealthBuildInfo(
          timestamp = Instant.now(),
          commit = "Initial commit",
          branch = "test",
          number = 123,
        ),
      ),
    )

    // When
    val actual = router(org.http4k.core.Request(Method.GET, "/health"))

    // Then
    actual shouldHaveStatus Status.OK
  }
}
