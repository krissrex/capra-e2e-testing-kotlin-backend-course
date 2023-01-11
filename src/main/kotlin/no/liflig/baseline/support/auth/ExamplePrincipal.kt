package no.liflig.baseline.support.auth

import kotlinx.serialization.Serializable
import no.liflig.logging.PrincipalLog

@JvmInline
value class FullName(val value: String)

@JvmInline
value class Email(val value: String) {
  init {
    require(value.contains('@'))
  }
}

data class ExamplePrincipal(
  val name: FullName,
  val email: Email,
  val roles: List<PrincipalRole>,
) {
  fun isAdmin() = PrincipalRole.ADMIN in roles
  fun isNormalUser() = PrincipalRole.NORMAL in roles
}

enum class PrincipalRole(val keycloakName: String) {
  ADMIN("admin"),
  NORMAL("vanlig"),
}

@Serializable
data class ExamplePrincipalLog(
  val name: String,
) : PrincipalLog

fun ExamplePrincipal.toLog() = ExamplePrincipalLog(name.value)
