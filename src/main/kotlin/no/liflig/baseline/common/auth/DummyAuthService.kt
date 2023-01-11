package no.liflig.baseline.common.auth

import arrow.core.Either
import arrow.core.right
import mu.KLogging
import no.liflig.http4k.AuthService
import no.liflig.http4k.GetPrincipalDeviation
import org.http4k.core.Request

object DummyAuthService : AuthService<ExamplePrincipal>, KLogging() {

  override suspend fun getPrincipal(request: Request): Either<GetPrincipalDeviation, ExamplePrincipal?> {
    logger.info { "Returning dummy principal" }
    return ExamplePrincipal(
      FullName("dummy"),
      Email("dummy@example.com"),
      PrincipalRole.values().toList(),
    ).countLogin().right()
  }

  private fun ExamplePrincipal.countLogin(): ExamplePrincipal {
    return this
  }
}
