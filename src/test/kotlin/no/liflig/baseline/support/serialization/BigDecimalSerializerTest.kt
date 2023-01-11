package no.liflig.baseline.support.serialization

import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class BigDecimalSerializerTest {
  @Test
  internal fun `should serialize to string`() {
    // Given
    val number = BigDecimal("16777217")

    // When
    val actual = Json.encodeToString(BigDecimalSerializer, number)

    // Then
    Assertions.assertThat(actual).isEqualTo("\"16777217\"")
  }
}
