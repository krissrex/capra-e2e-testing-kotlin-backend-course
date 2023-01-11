package no.liflig.mysampleservice

import no.liflig.snapshot.verifyJsonSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.util.Integration

class ExampleEndToEndTest {

  @Integration
  @Test
  fun `should return a thing from api`() {
    // Given
    // TODO start the app

    // When
    // TODO invoke API

    // Then
    assertThat("x").isEqualTo("x")

    verifyJsonSnapshot("example/api-response.json", """{"response": "ok"}""")

    TODO("Not yet implemented")
  }

  @Integration
  @Test
  fun `should process a message on sqs and produce something on sns`() {
    // Given
    // TODO start the app

    // When
    // TODO send a message to the queue

    // Then
    // TODO await a message out from SNS

    assertThat("y").isEqualTo("y")

    verifyJsonSnapshot("example/sns-message.json", """{"message": "hello world"}""")

    TODO("Not yet implemented")
  }

  @Integration
  @Test
  fun `should put something into a database`() {
    // Given
    // TODO start the app

    // When
    // TODO do something that results in a database insert

    // Then
    // TODO assert something exists in the database

    assertThat("z").isEqualTo("z")
    TODO("Not yet implemented")
  }
}
