# Course Material for End-to-End Testing a Kotlin Backend

<!-- 
Using this README template:
Replace any text <inside tags> with something that suits your project.
Remove any sections that do not fit.
Remove or modify the Badges with the correct links and artifact urls.
Update any visible text or links to Confluence etc. with your details.
Write the appropriate dependencies and steps for getting started.
-->

![Java Badge](https://img.shields.io/badge/java-17-blue?logo=java)
![Kotlin Badge](https://img.shields.io/badge/kotlin--blue?logo=kotlin)

Responsible for &lt;transforming source data into a domain model, persisting the data, publish updates to SNS topic for
subscribers and providing APIs for lookup of these entities.>

## Documentation

More information is found here:

- [Slideshow](https://docs.google.com/presentation/d/1t3tc1KePlF6EUdAyNJj3eaHl6DipFOLNx-kdugog6j0/edit?usp=sharing) (Google docs)

## Contributing

### Getting started

#### Tool dependencies

You need to install:

- Docker
- Maven (or run maven through IntelliJ)
- JDK 17
    - `brew tap homebrew/cask-versions` and then`brew install --cask temurin17`

#### Developer machine setup

##### GitHub Packages

Create a personal access token (classic) with at least `packages:read` scope:
https://github.com/settings/tokens/new?scopes=packages:read .
Copy the generated token.

Add a token to maven ( `~/.m2/settings.xml` ) for GitHub Packages:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      https://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <servers>
      <server>
        <id>github</id>
        <username>my-username-on-github</username>
        <password>123_abc-my-secret-token-here</password>
      </server>
  </servers>

</settings>
```

##### Git Clone

```shell
git clone git@github.com:krissrex/capra-e2e-testing-kotlin-backend-course.git
```

**IntelliJ** → `File` → `New` → `Project from existing sources...` → `capra-e2e-testing-kotlin-backend-course`.

Choose _"Import project from external model"_ and select `Maven`.

### Running the application

1. Build the jar: `mvn package`
2. Copy the jar from `target/app.jar` to `/docker/app.jar`.
   - You can use `cd docker && ./test-docker.sh`.
3. Run the app
   - Start `docker-compose`:
      ```shell
      docker-compose -f docker-compose.yml up -d --build
      ```
   - Or run `no.liflig.mysampleservice.Main.main()` 
   - Or `cd docker && ./test-docker.sh`

You can test the API with [src/test/http/health.http](src/test/http/health.http)

### Running tests

```shell
mvn verify
```

Add `-DskipTests` to `mvn` to disable all tests.  
Add `-DskipITs` to only disable integration tests.

### Linting

Only lint: `mvn ktlint:check`  

`mvn ktlint:ktlint` to create a report in `target/site/ktlint.html`.

Fix: `mvn ktlint:format`

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

