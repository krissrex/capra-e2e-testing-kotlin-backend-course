# Reference: Testcontainers

**Responsibility:** Start and stop Docker containers during a test.

https://www.testcontainers.org/quickstart/junit_5_quickstart/

The dependencies are specific to certain docker-containers, because they have pre-defined ports and settings.

Postgres (database):

```xml

<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <version>1.17.6</version>
  <scope>test</scope>
</dependency>
```

Localstack (AWS):

```xml

<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>localstack</artifactId>
  <version>1.17.6</version>
  <scope>test</scope>
</dependency>
<dependency>
<!-- Needed by localstack. -->
<groupId>com.amazonaws</groupId>
<artifactId>aws-java-sdk-core</artifactId>
<version>1.12.420</version>
<scope>test</scope>
</dependency>
```

## Usage

### Starting postgres

```kotlin
class Database {
  companion object {
    private lateinit var postgres: PostgreSQLContainer<*>
    private lateinit var jdbi: Jdbi
  }

  private fun initDatabase(config: Config): PostgreSQLContainer<*> {
    postgres = PostgreSQLContainer("postgres:14.2-alpine")
    postgres.withDatabaseName("app").withUsername("test").withPassword("test").start()

    postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)

    config.database =
      DbConfig(
        username = "test",
        password = "test",
        dbname = "app",
        port = postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
        hostname = "localhost",
        jdbcUrl = postgres.jdbcUrl
      )

    val databaseConnection = DatabaseConnection(config.database)
    databaseConnection.initialize()
    jdbi = databaseConnection.jdbi

    return postgres
  }
}

```

### Stopping postgres

Remember to call `postgres.stop()`.

It's not a big deal; [Ryuk](https://github.com/testcontainers/moby-ryuk) will remove the container anyways.

### Starting Localstack

**Responsibility:** Simulate Amazon Web Services (AWS). Topics, queues.

https://www.testcontainers.org/modules/localstack/

```kotlin
class Localstack {

  companion object {
    private val localStackContainer: LocalStackContainer
  }

  fun initLocalstack() {
    val localstackImage: DockerImageName = DockerImageName.parse("localstack/localstack:1.4.0")
    localStackContainer =
      new LocalStackContainer
        (localstackImage).withServices(
          LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS
        )

    localStackContainer.start()
  }
}

```

### Granting a container access to your service/machine

Used for services that call back, like SNS HTTP subscriptions.

```kotlin
localStackContainer = LocalStackContainer(DockerImageName.parse("localstack/localstack:1.4.0"))
  .withAccessToHost(true) // Required for SNS to deliver messages to server outside of docker
  .withServices(LocalStackContainer.Service.SNS)

```
