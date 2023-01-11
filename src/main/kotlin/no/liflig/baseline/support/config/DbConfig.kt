package no.liflig.baseline.support.config

import no.liflig.properties.intRequired
import no.liflig.properties.stringNotNull
import java.util.Properties

data class DbConfig(
  val username: String,
  val password: String,
  val dbname: String,
  private val port: Int,
  private val hostname: String,
  val jdbcUrl: String = "jdbc:postgresql://$hostname:$port/$dbname",
) {

  companion object {
    /**
     * Reads in database values that are set from an AWS Secrets Manager json and placed into a properties file.
     */
    fun create(properties: Properties): DbConfig {
      // The properties keys must match the json in AWS Secrets Manager for ProductDatabaseSecret
      val username = properties.stringNotNull("database.username")
      val password = properties.stringNotNull("database.password")
      val port = properties.intRequired("database.port")
      val dbname = properties.stringNotNull("database.dbname")
      val hostname = properties.stringNotNull("database.host")

      return DbConfig(username, password, dbname, port, hostname)
    }
  }
}
