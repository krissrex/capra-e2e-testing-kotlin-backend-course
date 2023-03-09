# Course Material for End-to-End Testing a Kotlin Backend

![Java Badge](https://img.shields.io/badge/java-17-blue?logo=java)
![Kotlin Badge](https://img.shields.io/badge/kotlin--blue?logo=kotlin)

**Example case:** Bartender Server. 🍻

Responsible for:

1. Providing a menu of drinks on a REST API
2. Collecting incoming orders from a message queue
3. Persisting orders in a database for bookkeeping
4. Collecting payment for orders using a payment-provider
5. Sending events to a topic when an order is ready

## Documentation

More information is found here:

- [Slideshow](https://docs.google.com/presentation/d/1t3tc1KePlF6EUdAyNJj3eaHl6DipFOLNx-kdugog6j0/edit?usp=sharing) (
  Google docs)

### Architecture

![component architecture](component-architecture.drawio.svg)

You can see that the 5 responsibilities also require 4 external systems (queue, payments, database, topic),
and it provides a service for external users (REST-API).

We need an end-to-end tests to have *some* confidence that our backend works with these external systems.
Mocking them in tests will not be good enough.

(For *real confidence*, you can go even further and create **system tests**, where you inject data into a real, deployed
AWS account as part of your CD-pipeline. This is out of scope for this course.)

#### Organization of code

![code architecture](code-architecture.drawio.svg)

The `Main`-file starts the application.
It loads a `Config` into an `App`.
The app starts serving the REST-API, and starts an `SqsPoller` to read from its SQS queue.

The API provides the output of `GetDrinkMenuRoute`.

The SQS poller loops forever, fetching new messages and sending them to a queue processor named `OrderQueueProcessor`.
The `OrderQueueProcessor` coordinates several services and persists to the database.
Then, an event is sent out using `OrderReadyNotifyer`.

### What is missing?

The code largely works (I think? No tests to verify...).

The tests, however, are missing.

**TODO-list:**

1. Test-setup

- Database: Testcontainers Postgres
- AWS SQS and SNS: Testcontainers Localstack
- Payments provider: Wiremock
- Tweak config with correct values

2. [OrderRepositoryTes](src/test/kotlin/no/liflig/bartenderservice/orders/OrderRepositoryTest.kt) needs some assertions.

- AssertJ
- verifyJsonSnapshot

3. [ApiEndToEndTest](src/test/kotlin/no/liflig/bartenderservice/ApiEndToEndTest.kt) needs some assertions.

- RestAssured
- verifyJsonSnapshot

4. [OrderProcessingEndToEndTest](src/test/kotlin/no/liflig/bartenderservice/OrderProcessingEndToEndTest.kt) needs some
   input and assertions.

- Inspect the SNS published messages: Awaitility + http client + localstack API
- Storage: database SELECT
- Payment provider: Wiremock verify
- Inspect SQS available messages count == 0

See the docs in [docs/](docs/index.md) for references on testing-llibraries used.

---

## Getting started

### Tool dependencies

You need to install:

- Docker
- Maven (or run maven through IntelliJ)
- JDK 17
  - `brew tap homebrew/cask-versions` and then`brew install --cask temurin17`

### Developer machine setup

#### Git Clone

```shell
git clone git@github.com:krissrex/capra-e2e-testing-kotlin-backend-course.git
```

**IntelliJ** → `File` → `New` → `Project from existing sources...` → `capra-e2e-testing-kotlin-backend-course`.

Choose _"Import project from external model"_ and select `Maven`.

## Running the application

Run `./init-local-env.sh` once to create `overrides.properties` to tweak settings.

Start [Main.kt](src/main/kotlin/no/liflig/bartenderservice/Main.kt).
You should use `./scripts/build.sh`, then `./scripts/run.sh` or `./scripts/run-docker.sh`.
Or run `no.liflig.bartenderservice.Main.main()` from IntelliJ.

The app needs a Postgres database, an SQS message queue, and an SNS pub/sub topic.

### Building a dockerfile (optional)

1. Build the jar: `mvn package`
2. Copy the jar from `target/app.jar` to `/docker/app.jar`.

- You can use `./scripts/build.sh`.

3. Run the app

- Start `docker-compose`:
   ```shell
   docker-compose -f docker-compose.yml up -d
   ```
- Or `./scripts/run-docker.sh`

You can test the API with [src/test/http/menu.http](src/test/http/menu.http)

### Sending test data

```shell
AWS_ACCESS_KEY_ID=x AWS_SECRET_ACCESS_KEY=x aws --region=us-east-1 --endpoint-url=http://localhost:4566 sqs send-message --queue-url=http://localstack:4566/000000000000/orders-queue --message-body '{"orderId":"1","customer":{"id":"1","age":"TWENTY"},"paymentInfo":{"cardNumber":"123-1234-123-456"},"orderLines":[{"id":"1","name":"Hansa","price":"98","size":"0.6","ageLimit":"EIGHTEEN"},{"id":"4","name":"Hakkespett","price":"120","size":"0.4","ageLimit":"TWENTY"}]}'
```

or

```shell
awslocal sqs send-message --queue-url=http://localstack:4566/000000000000/orders-queue --message-body '{"orderId":"1","customer":{"id":"1","age":"TWENTY"},"paymentInfo":{"cardNumber":"123-1234-123-456"},"orderLines":[{"id":"1","name":"Hansa","price":"98","size":"0.6","ageLimit":"EIGHTEEN"},{"id":"4","name":"Hakkespett","price":"120","size":"0.4","ageLimit":"TWENTY"}]}'
```

## Running tests

```shell
mvn verify
```

Add `-DskipTests` to `mvn` to disable all tests.
Add `-DskipITs` to only disable integration tests.

## Linting

Only lint: `mvn spotless:check`

Fix: `mvn spotless:apply`

## License

```text
Copyright 2022 Liflig By Capra AS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
