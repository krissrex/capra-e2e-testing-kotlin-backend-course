package no.liflig.bartenderservice.orders

import no.liflig.bartenderservice.drinkmenu.AgeLimit
import no.liflig.bartenderservice.drinkmenu.Drink
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class AgeLimitPolicyTest {
  @Test
  fun `should complain when 18 year old user buys 20yo limit drink`() {
    // Given
    val policy = AgeLimitPolicy()
    val order =
        DrinkOrder(
            "1",
            Customer("1", AgeLimit.EIGHTEEN),
            PaymentInfo(""),
            listOf(
                Drink("1", "Sprit", "100", "0.04", AgeLimit.TWENTY),
            ),
        )

    // When
    Assertions.assertThatThrownBy { policy.enforceAgeLimitPolicy(order) }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `should complain when underage user buys 20yo limit drink`() {
    // Given
    val policy = AgeLimitPolicy()
    val order =
        DrinkOrder(
            "1",
            Customer("1", AgeLimit.NONE),
            PaymentInfo(""),
            listOf(
                Drink("1", "Øl", "100", "0.04", AgeLimit.EIGHTEEN),
            ),
        )

    // When
    Assertions.assertThatThrownBy { policy.enforceAgeLimitPolicy(order) }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `should allow 20 year old user to buy any drink`() {
    // Given
    val policy = AgeLimitPolicy()
    val order =
        DrinkOrder(
            "1",
            Customer("1", AgeLimit.TWENTY),
            PaymentInfo(""),
            listOf(
                Drink("1", "Vann", "100", "0.04", AgeLimit.NONE),
                Drink("1", "Øl", "100", "0.04", AgeLimit.EIGHTEEN),
                Drink("1", "Sprit", "100", "0.04", AgeLimit.TWENTY),
            ),
        )

    // When
    policy.enforceAgeLimitPolicy(order)
  }
}
