package no.liflig.bartenderservice.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds
import mu.KotlinLogging
import no.liflig.bartenderservice.common.config.DbConfig
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi

private val log = KotlinLogging.logger {}

class DatabaseConnection(private val dbConfig: DbConfig) {

  lateinit var jdbi: Jdbi
    private set
  fun initialize() {
    val config = createDatasourceConfig()
    val dataSource = HikariDataSource(config)
    addJvmShutdownHookToCleanUp(dataSource)

    migrate(dataSource)

    jdbi = Jdbi.create(dataSource)
  }

  private fun migrate(dataSource: DataSource) {
    val flyway =
        Flyway.configure()
            .baselineOnMigrate(true)
            .dataSource(dataSource)
            .locations("migrations")
            .validateMigrationNaming(true)
            .load()
    log.debug { "Running database migrations..." }
    flyway.migrate()
  }

  private fun createDatasourceConfig(): HikariConfig {
    val config = HikariConfig()
    config.driverClassName = "org.postgresql.Driver"
    config.jdbcUrl = dbConfig.jdbcUrl
    config.username = dbConfig.username
    config.password = dbConfig.password

    config.connectionTimeout = 50.seconds.inWholeMilliseconds
    config.idleTimeout = 30.seconds.inWholeMilliseconds

    config.minimumIdle = 1
    config.maximumPoolSize = 4
    return config
  }

  private fun addJvmShutdownHookToCleanUp(dataSource: HikariDataSource) {
    try {
      Runtime.getRuntime()
          .addShutdownHook(
              thread(start = false, name = "hikari-shutdown") {
                if (!dataSource.isClosed) {
                  dataSource.close()
                }
              },
          )
    } catch (e: Exception) {
      log.error(e) { "Failed to add shutdown hook" }
    }
  }
}
