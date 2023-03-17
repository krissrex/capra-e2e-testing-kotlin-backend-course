package no.liflig.bartenderservice

import io.restassured.RestAssured
import no.liflig.bartenderservice.common.config.Config
import no.liflig.snapshot.verifyJsonSnapshot
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import test.util.Integration

@ExtendWith(EndToEndTestExtension::class)
class ApiEndToEndTest {
  @Integration
  @Test
  fun `should return a thing from api`(config: Config) {
    // Given

    App(config).start()

    // When
    val responseBody =
        RestAssured.`when`()
            .get("/api/v1/menu")
            .then()
            .statusCode(200)
            .log()
            .ifError()
            .extract()
            .asPrettyString()

    // Then
    verifyJsonSnapshot("example/api-response.json", responseBody)
  }
}
