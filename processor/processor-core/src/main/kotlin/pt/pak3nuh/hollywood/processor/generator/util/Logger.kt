package pt.pak3nuh.hollywood.processor.generator.util

import javax.annotation.processing.Messager
import javax.tools.Diagnostic

class Logger(private val messager: Messager, val debug: Boolean = true) {
    fun logInfo(message: String) {
        log(message)
    }

    fun logDebug(message: String) {
        if (debug) {
            log(message)
        }
    }

    private fun log(message: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, "$message$separator")
    }

    private companion object {
        val separator: String = System.lineSeparator()
    }
}
