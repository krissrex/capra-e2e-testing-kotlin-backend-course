# Reference: Awaitility

**Responsibility:** Wait for a condition, or time out.

http://www.awaitility.org/

```xml

<dependency>
  <groupId>org.awaitility</groupId>
  <artifactId>awaitility</artifactId>
  <version>4.2.0</version>
  <scope>test</scope>
</dependency>
```

## Usage

### Minimal

```kotlin
Awaitility.await().until { true }
```

### Waiting for a Wiremock server call

```kotlin
Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted {
  myExternalApiWiremockServer.verify(
    postRequestedFor(urlEqualTo("/api/example/something"))
      .withRequestBody(
        equalToJson(
          loadTestFile("/e2e-test/my-service.create-something-request-1.json"),
        ),
      ),
  )
}
```

### Waiting for something to exist in a database

```kotlin
Awaitility.await().untilAsserted {
  val order =
    jdbi.withHandle<DrinkOrder, Exception> { handle ->
      val order: JsonObject =
        handle.select(
          "SELECT * FROM orders WHERE (data->>'id') = ? LIMIT 1", "$orderId"
        ).map { rs, _ ->
          Json.decodeFromString(DrinkOrder.serializer(), rs.getString("data"))
        }.first()

      order
    }

  assertThat(order).isNotNull
  assertThat(order.customer.id)
    .isNotNull
    .isEqualTo("2")
}

```
