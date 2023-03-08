package no.liflig.bartenderservice.common.serialization

import org.http4k.format.ConfigurableKotlinxSerialization
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings

object PrettyHttp4kKotlinxSerialization :
    ConfigurableKotlinxSerialization({
      ignoreUnknownKeys = true
      prettyPrint = true
      asConfigurable().withStandardMappings().done()
    })
