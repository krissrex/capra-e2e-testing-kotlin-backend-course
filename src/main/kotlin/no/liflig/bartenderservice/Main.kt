package no.liflig.bartenderservice

import kotlin.system.exitProcess
import no.liflig.bartenderservice.common.config.Config

fun main(args: Array<String>) {
  if ("--create-sample-order" in args) {
    printExampleOrder()
    exitProcess(0)
  }

  App(Config.load()).start()
}
