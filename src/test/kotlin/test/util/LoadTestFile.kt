package test.util

import kotlin.jvm.Throws

/**
 * Load a file from the resources folder.
 *
 * @param path filename in resources, like
 *   `"/sample-orders/1_customer-aged20-buys-beer-limited18-and-spirits-limited20.json"`
 */
@Throws(IllegalArgumentException::class)
fun loadTestFile(path: String): String {
  return object {}::class.java.getResource(path)!!.readText()
}
