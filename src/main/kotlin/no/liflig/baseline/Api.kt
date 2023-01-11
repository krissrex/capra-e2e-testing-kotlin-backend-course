package no.liflig.baseline

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.liflig.baseline.subdomain.api.ExampleAggregateEndpoints
import no.liflig.baseline.support.config.Config
import no.liflig.logging.http4k.ErrorResponseRendererWithLogging
import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.ApiRenderer
import org.http4k.contract.openapi.cached
import org.http4k.contract.openapi.v3.Api
import org.http4k.contract.openapi.v3.ApiServer
import org.http4k.contract.openapi.v3.AutoJsonToJsonSchema
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.security.BasicAuthSecurity
import org.http4k.core.Uri
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

fun api(config: Config, errorResponseRenderer: ErrorResponseRendererWithLogging): RoutingHttpHandler {
  return "api" / "v1" bind contract {
    val jacksonJson = JacksonJson
    renderer = OpenApi3(
      apiInfo = ApiInfo(
        title = config.applicationName,
        version = "1",
        description = "A REST API for my service",
      ),
      json = jacksonJson,
      apiRenderer = ApiRenderer.Auto<Api<JsonNode>, JsonNode>(
        jacksonJson,
        schema = AutoJsonToJsonSchema(jacksonJson),
      ).cached(),
      errorResponseRenderer = errorResponseRenderer,
      servers = listOf(ApiServer(Uri.of("http://localhost:${config.serverPort}"))),
    )
    descriptionPath = "/api-docs"
    descriptionSecurity = BasicAuthSecurity("master", config.openapiCredentials)

    routes += ExampleAggregateEndpoints().routes
  }
}

object JacksonJson : ConfigurableJackson(
  KotlinModule.Builder()
    .withReflectionCacheSize(512)
    .configure(KotlinFeature.NullToEmptyCollection, false)
    .configure(KotlinFeature.NullToEmptyMap, false)
    .configure(KotlinFeature.NullIsSameAsDefault, false)
    .configure(KotlinFeature.SingletonSupport, false)
    .configure(KotlinFeature.StrictNullChecks, false)
    .build()
    .asConfigurable(
      JsonMapper.builder()
        .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
        .build(),
    )
    .withStandardMappings()
    .done()
    .deactivateDefaultTyping()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true),
)

interface Endpoint {
  val routes: Collection<ContractRoute>
}

interface Route {
  val route: ContractRoute
}
