package no.liflig.bartenderservice.drinkmenu

import kotlinx.serialization.Serializable

@Serializable
data class DrinkMenu(
    val drinks: List<Drink>,
) {
  companion object {
    fun createDefaultMenu() =
        DrinkMenu(
            drinks =
                listOf(
                    Drink("1", "Hansa", "98", "0.6", AgeLimit.EIGHTEEN),
                    Drink("2", "Dahls", "65", "0.5", AgeLimit.EIGHTEEN),
                    Drink("3", "Liflig-pils", "20", "0.5", AgeLimit.EIGHTEEN),
                    Drink("4", "Hakkespett", "120", "0.4", AgeLimit.TWENTY),
                    Drink("5", "Munkholm", "40", "0.5", AgeLimit.NONE),
                ),
        )
  }
}

@Serializable
data class Drink(
    val id: String,
    val name: String,
    /** In kroner. Like `100` or `60.20`. */
    val price: String,
    /** In liter. Like `0.5`, `0.6`. */
    val size: String,
    val ageLimit: AgeLimit,
)

@Serializable
enum class AgeLimit {
  NONE,
  EIGHTEEN,
  TWENTY,
}
