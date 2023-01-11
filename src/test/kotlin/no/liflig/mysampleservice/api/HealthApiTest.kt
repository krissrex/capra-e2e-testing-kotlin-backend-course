package no.liflig.mysampleservice.api

import no.liflig.http4k.health.HealthBuildInfo
import no.liflig.http4k.health.HealthService
import no.liflig.mysampleservice.common.auth.DummyAuthService
import no.liflig.mysampleservice.common.config.Config
import no.liflig.mysampleservice.createApi
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.Test
import java.time.Instant

class HealthApiTest {

  @Test
  internal fun `health should respond 200 ok`() {
    // Given
    val router = createApi(
      logHandler = { },
      config = Config.load(),
      authService = DummyAuthService,
      healthService = HealthService(
        name = "Test-app",
        buildInfo = HealthBuildInfo(
          timestamp = Instant.now(),
          commit = "Initial commit",
          branch = "master",
          number = 123,
        ),
      ),
    )

    // When
    val actual = router(Request(Method.GET, "/health"))

    // Then
    actual shouldHaveStatus Status.OK
  }
}
