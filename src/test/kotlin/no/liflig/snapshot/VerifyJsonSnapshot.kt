/**
 * Copyright Liflig by Capra AS under Apache 2.0 license. 2023. Copied from
 * https://github.com/capralifecycle/snapshot-test.
 */
package no.liflig.snapshot

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
  prettyPrint = true
  prettyPrintIndent = "  "
}

private fun produceJsonErrors(previous: String, current: String): String {
  val jsonCompareResult = JSONCompare.compareJSON(previous, current, JSONCompareMode.STRICT)
  return "Error(s):\n$jsonCompareResult"
}

private fun assertJsonSnapshot(
    existingValue: String,
    newValue: String,
    ignoredPaths: List<String>? = null
) {
  val compareMode = JSONCompareMode.STRICT
  if (ignoredPaths != null) {
    JSONAssert.assertEquals(
        existingValue,
        newValue,
        CustomComparator(
            compareMode,
            *ignoredPaths
                .map { Customization(it) { _: Any?, o2: Any? -> o2 != null } }
                .toTypedArray()))
  } else {
    JSONAssert.assertEquals(existingValue, newValue, compareMode)
  }
}

/** Ensure that the serialized JSON matches an existing snapshot. */
@JvmOverloads
fun verifyJsonSnapshot(name: String, value: JsonElement, ignoredPaths: List<String>? = null) {
  val prettified = json.encodeToString(JsonElement.serializer(), value) + "\n"
  verifySnapshot(name, prettified, ::produceJsonErrors) { existingValue: String, newValue: String ->
    assertJsonSnapshot(existingValue, newValue, ignoredPaths)
  }
}

/**
 * Verify that the value matches the snapshot. The value must be a valid JSON string and will be
 * reformatted. Use [verifyStringSnapshot] to check for whitespace.
 */
@JvmOverloads
fun verifyJsonSnapshot(name: String, value: String, ignoredPaths: List<String>? = null) {
  verifyJsonSnapshot(name, json.parseToJsonElement(value), ignoredPaths)
}
