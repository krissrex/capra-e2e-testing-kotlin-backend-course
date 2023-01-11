package no.liflig.baseline.support.auth

import arrow.core.Either
import arrow.core.right
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.LongCounter
import mu.KLogging
import no.liflig.baseline.support.observability.AuthMetrics
import no.liflig.http4k.AuthService
import no.liflig.http4k.GetPrincipalDeviation
import org.http4k.core.Request

object DummyAuthService : AuthService<ExamplePrincipal>, KLogging() {
  private val loginCounter: LongCounter = AuthMetrics.logins()

  override suspend fun getPrincipal(request: Request): Either<GetPrincipalDeviation, ExamplePrincipal?> {
    logger.info { "Returning dummy principal" }
    return ExamplePrincipal(
      FullName("dummy"),
      Email("dummy@example.com"),
      PrincipalRole.values().toList(),
    ).countLogin().right()
  }

  private fun ExamplePrincipal.countLogin(): ExamplePrincipal {
    val attributes = Attributes.of(
      AttributeKey.stringArrayKey("roles"),
      this.roles.map { it.name },
      AttributeKey.booleanKey("is_admin"),
      this.isAdmin(),
    )
    loginCounter.add(1, attributes)

    return this
  }
}
