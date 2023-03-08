package no.liflig.bartenderservice.orders

import java.sql.ResultSet
import no.liflig.bartenderservice.common.database.DatabaseConnection
import no.liflig.bartenderservice.drinkmenu.AgeLimit
import no.liflig.bartenderservice.drinkmenu.Drink
import org.assertj.core.api.Assertions
import org.jdbi.v3.core.statement.StatementContext
import org.junit.jupiter.api.Test
import test.util.Integration

class OrderRepositoryTest {

  @Integration
  @Test
  fun `should put something into a database`() {
    // Given
    val testDbConfig = TODO() // DbConfig()
    val db = DatabaseConnection(testDbConfig)
    db.initialize()

    val repo = OrderRepository(db.jdbi)

    val order =
        DrinkOrder(
            orderId = "123",
            customer = Customer("2", AgeLimit.EIGHTEEN),
            paymentInfo = PaymentInfo("123-123-123"),
            orderLines = listOf(Drink("3", "Test-pils", "50.00", "0.5", AgeLimit.EIGHTEEN)))

    // When
    repo.createOrder(order)

    // Then
    val fromDatabase =
        db.jdbi.withHandle<List<DrinkOrder>, Exception> { handle ->
          handle
              .createQuery("SELECT * FROM orders")
              .map { row: ResultSet, _: StatementContext ->
                DrinkOrder.fromJson(row.getString("data"))
              }
              .list()
        }
    // TODO assert something exists in the database

    Assertions.assertThat("z").isEqualTo("z")
    TODO("Not yet implemented")
  }
}
