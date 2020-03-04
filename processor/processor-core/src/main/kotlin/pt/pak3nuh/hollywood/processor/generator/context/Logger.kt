package pt.pak3nuh.hollywood.processor.generator.context

import javax.annotation.processing.Messager
import javax.tools.Diagnostic

class Logger(private val messager: Messager) {
    fun logInfo(message: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, message)
    }

    fun logDebug(message: String) {
        messager.printMessage(Diagnostic.Kind.OTHER, message)
    }
}