# Reference: RestAssured

**Responsibility:** Invoke your own API. Assert on the response.

https://github.com/rest-assured/rest-assured/wiki/Usage#examples

```xml

<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>rest-assured</artifactId>
  <version>5.3.0</version>
  <scope>test</scope>
  <exclusions>
    <exclusion>
      <groupId>commons-logging</groupId>
      <artifactId>commons-logging</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

## Usage

### Calling an API

RestAssured will by default use `http://localhost:8080`.
The function `when` has backticks to avoid collision with the Kotlin keyword.

```kotlin
RestAssured.`when`().get(path)
  .then().statusCode(200)
  .log().ifError()
```

### Asserting responses

You can re-use a set of assertions with
a [ResponseSpecification](https://github.com/rest-assured/rest-assured/wiki/Usage#specification-re-use).

To get the actual response body out from RestAssured, use `extract().asPrettyString()`.

```kotlin
val spec =
  ResponseSpecBuilder()
    .expectBody(Matchers.not(Matchers.emptyString()))
    .expectContentType("application/json")
    .build()

val responseBody =
  RestAssured.`when`().get("/api/v1/menu")
    .then()
    .statusCode(200)
    .log().ifError()
    .spec(spec)
    .extract().asPrettyString()

// Now do tests on the body, like:
// verifyJsonSnapshot("example/api-response.json", responseBody)

```

#### Body assertions

Use dot-notation on the body JSON, and [Hamcrest](https://hamcrest.org/JavaHamcrest/tutorial) matchers to compare.

```kotlin
get("/lotto").then().body("lotto.lottoId", equalTo(5))

get("/lotto").then().body("lotto.winners.winnerId", hasItems(23, 54))
```
