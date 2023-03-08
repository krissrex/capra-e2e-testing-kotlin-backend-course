package no.liflig.bartenderservice

import no.liflig.snapshot.verifyJsonSnapshot
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import test.util.Integration

class ApiEndToEndTest {
  @Integration
  @Test
  fun `should return a thing from api`() {
    // Given
    // TODO start the app

    // When
    // TODO invoke API

    // Then
    Assertions.assertThat("x").isEqualTo("x")

    verifyJsonSnapshot("example/api-response.json", """{"response": "ok"}""")

    TODO("Not yet implemented")
  }
}
