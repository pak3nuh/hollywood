package pt.pak3nuh.hollywood.processor.generator.util

import javax.annotation.processing.Messager
import javax.tools.Diagnostic

class Logger(private val messager: Messager, val debug: Boolean = true) {
    fun logInfo(message: String) {
        log(Diagnostic.Kind.NOTE, message)
    }

    fun logDebug(message: String) {
        if (debug) {
            log(Diagnostic.Kind.OTHER, message)
        }
    }

    private fun log(kind: Diagnostic.Kind, message: String) {
        messager.printMessage(kind, "$message$separator")
    }

    fun logError(exception: Exception) {
        val msg = "Exception ${exception::class.simpleName}: ${exception.message}"
        log(Diagnostic.Kind.ERROR, msg)
    }

    private companion object {
        val separator: String = System.lineSeparator()
    }
}
