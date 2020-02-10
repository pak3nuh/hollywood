package pt.pakenuh.hollywood.sandbox.actor

import kotlinx.coroutines.delay
import kotlin.random.Random


object Randoms {
    private val random = Random(0)

    fun int(until: Int): Int = random.nextInt(until)

    suspend fun delay(until: Int) {
        delay(int(until).toLong())
    }

    fun bool(): Boolean = random.nextBoolean()
}

