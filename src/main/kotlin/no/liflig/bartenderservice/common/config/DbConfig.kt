package no.liflig.bartenderservice.common.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.int
import org.http4k.lens.string

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
     * Reads in database values that are set from an AWS Secrets Manager json and placed into a
     * properties file.
     */
    fun create(env: Environment): DbConfig {
      val username = username(env)
      val password = password(env)
      val port = port(env)
      val dbname = dbname(env)
      val hostname = hostname(env)

      return DbConfig(username, password, dbname, port, hostname)
    }
  }
}

private val username = EnvironmentKey.string().required("database.username")
private val password = EnvironmentKey.string().required("database.password")
private val port = EnvironmentKey.int().required("database.port")
private val dbname = EnvironmentKey.string().required("database.dbname")
private val hostname = EnvironmentKey.string().required("database.host")
