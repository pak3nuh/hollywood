package pt.pak3nuh.hollywood.sandbox

import org.slf4j.LoggerFactory

object Loggers {

    fun getLogger(name: String): org.slf4j.Logger = LoggerFactory.getLogger(name)

    inline fun <reified T> getLogger(): org.slf4j.Logger = getLogger(T::class.java.name)
}
