/**
 * Copyright Liflig by Capra AS under Apache 2.0 license. 2023. Copied from
 * https://github.com/capralifecycle/snapshot-test.
 */
package no.liflig.snapshot

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import java.io.File
import kotlin.test.assertEquals

internal const val REGENERATE_SNAPSHOTS = "REGENERATE_SNAPSHOTS"
internal const val REGENERATE_FAILED_SNAPSHOTS = "REGENERATE_FAILED_SNAPSHOTS"

/**
 * Check that we are running using the expected working directory.
 *
 * In IntelliJ if using a multi-module Maven project, the working directory will be the workspace
 * directory and not each module directory. This must be changed in test configuration in IntelliJ.
 */
private fun checkExpectedWorkingDirectory() {
  check(File("src/test/resources").exists()) {
    "The tests are likely running with wrong working directory. src/test/resources was not found"
  }
}

private val shouldRegenerateAll: Boolean
  get() = System.getProperty(REGENERATE_SNAPSHOTS)?.toBoolean() ?: false

private val shouldRegenerateFailed: Boolean
  get() = System.getProperty(REGENERATE_FAILED_SNAPSHOTS)?.toBoolean() ?: false

private fun createDiff(
    original: List<String>,
    new: List<String>,
): String {
  val patch = DiffUtils.diff(original, new)
  val diff = UnifiedDiffUtils.generateUnifiedDiff(null, null, original, patch, 10)
  // Remove "diff header" and replace lines with only whitespace with empty lines
  // to make it easier to check in tests.
  return diff.drop(3).joinToString("\n") { it.ifBlank { "" } }
}

/**
 * Find and read previous snapshot and compare the new and old values to match exactly.
 *
 * The snapshot named [name] will be stored in src/test/resources/__snapshots__ and might contain a
 * subdirectory, e.g. "subdir/test.json".
 *
 * The [getExtra] parameter is for local library usage.
 */
@JvmOverloads
fun verifyStringSnapshot(
    name: String,
    value: String,
    getExtra: ((previous: String, current: String) -> String?)? = null,
) {
  verifySnapshot(name, value, getExtra) { existingValue: String, newValue: String ->
    assertEquals(existingValue, newValue)
  }
}

internal fun verifySnapshot(
    name: String,
    value: String,
    getExtra: ((previous: String, current: String) -> String?)? = null,
    assertSnapshot: (String, String) -> Unit,
) {
  checkExpectedWorkingDirectory()

  val resource = File("src/test/resources/__snapshots__", name)
  resource.parentFile.mkdirs()

  val snapshotExists = resource.exists()

  if (!snapshotExists) {
    println("[INFO] Snapshot for [$name] does not exist, creating")
    resource.writeText(value)
    return
  }

  val existingValue = resource.readText()

  if (shouldRegenerateAll) {
    if (existingValue == value) {
      println("[INFO] Existing snapshot for [$name] OK.")
    } else {
      println("[INFO] Snapshot for [$name] does not match, regenerating")
      resource.writeText(value)
    }
    return
  }

  if (shouldRegenerateFailed) {
    try {
      assertSnapshot(existingValue, value)
      println("[INFO] Existing snapshot for [$name] OK.")
    } catch (e: AssertionError) {
      println("[INFO] Snapshot for [$name] not OK, regenerating")
      resource.writeText(value)
    }
    return
  }

  try {
    assertSnapshot(existingValue, value)
  } catch (e: AssertionError) {
    val diff = createDiff(existingValue.lines(), value.lines())

    val extra = getExtra?.invoke(existingValue, value)?.let { "$it\n\n" } ?: ""

    System.err.println(
        """
#####################################################################

Snapshot [$name] failed - recreate all snapshots by setting system property $REGENERATE_SNAPSHOTS to true
Example: mvn test -DREGENERATE_SNAPSHOTS=true
Only recreate failed snapshots by setting system property $REGENERATE_FAILED_SNAPSHOTS to true instead
Example: mvn test -DREGENERATE_FAILED_SNAPSHOTS=true

${extra}Diff:

$diff

#####################################################################
      """
            .trimIndent())

    throw AssertionError("Snapshot [$name] doesn't match")
  }
}
