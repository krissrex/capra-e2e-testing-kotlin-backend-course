# Reference: Wiremock

**Responsibility:** Simulate an external API. Respond to calls. Capture calls for later assertion.

https://wiremock.org/docs/java-usage/

```xml

<dependency>
  <groupId>com.github.tomakehurst</groupId>
  <artifactId>wiremock-jre8-standalone</artifactId>
  <version>2.35.0</version>
  <scope>test</scope>
</dependency>
```

## Usage

### Starting a server

```kotlin
val myExternalApi: WireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(6789))
myExternalApi.start()

// Now available on http://localhost:6789/
```

### Mocking state

```kotlin
myExternalApi.stubFor(
  get(urlPathEqualTo("/example/endpoint/hello/123"))
    .willReturn(okJson(loadTestFile("/e2e-test/myexternal-api-hello.response-123.json"))),
)

wireMockServers.myExternalApi.stubFor(
  get(urlPathEqualTo("/example/endpoint/hello/111/asdf"))
    .withQueryParam("start", matching(""".*"""))
    .withQueryParam("end", matching(""".*"""))
    .willReturn(okJson(loadTestFile("/e2e-test/myexternal-api-hello.response-111.json"))),
)

myExternalApi.stubFor(
  post("/create/something/with-location")
    .willReturn(
      status(202)
        .withHeader(
          "Location",
          "http://localhost:${myExternalApi.port()}/request-status/v1/id/1",
        ),
    ),
)
```

### Resetting state

```kotlin
myExternalApi.resetAll()
```

### Asserting calls

```kotlin
myExternalApi.verify(
  postRequestedFor(urlEqualTo("/example/endpoint/something/1"))
    .withHeader("Authorization", equalTo("Bearer 123"))
    .withRequestBody(
      equalToJson(resource("/e2e-test/example.something-request-1.json"))
    ),
)
```

```kotlin
val actualRequestBody: String =
  myExternalApi
    .findAll(WireMock.postRequestedFor(urlEqualTo("/example/endpoint/something")))[0]
    .actualRequestBody

verifyJsonSnapshot("something/create-something-result.json", actualRequestBody)

```

### Stopping the server

```kotlin
myExternalApi.stop()
```
