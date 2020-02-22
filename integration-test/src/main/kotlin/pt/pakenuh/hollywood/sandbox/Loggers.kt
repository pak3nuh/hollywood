package pt.pakenuh.hollywood.sandbox

import java.util.logging.Level
import java.util.logging.Logger

object Loggers {
    private val logLevel = Level.FINE
    fun getLogger(name: String): Logger = Logger.getLogger(name).apply { level = logLevel }
    inline fun <reified T> getLogger(): Logger = getLogger(T::class.java.name)
}
