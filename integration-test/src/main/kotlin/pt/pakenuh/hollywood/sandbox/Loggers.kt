package pt.pakenuh.hollywood.sandbox

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.logging.*

object Loggers {

    private val logLevel = Level.FINE

    init {
        val rootLogger = Logger.getLogger("")
        rootLogger.level = logLevel
        val consoleHandler = rootLogger.handlers.first { it is ConsoleHandler }
        consoleHandler.level = logLevel
        consoleHandler.formatter = object : Formatter() {
            override fun format(record: LogRecord): String {
                val date = Instant.ofEpochMilli(record.millis)
                val loggerName = record.loggerName.split('.').last()
                return String.format("%s %s %s: %s${System.lineSeparator()}", date, loggerName, record.level, record.message)
            }
        }
    }

    fun getLogger(name: String): Logger = Logger.getLogger(name)

    inline fun <reified T> getLogger(): Logger = getLogger(T::class.java.name)
}
