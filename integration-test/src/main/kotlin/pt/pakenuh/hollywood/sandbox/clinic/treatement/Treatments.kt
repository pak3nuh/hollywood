package pt.pakenuh.hollywood.sandbox.clinic.treatement

import kotlin.random.Random

sealed class Treatment(val cost: Int, private val successRate: Int) {
    init {
        require(successRate in 1 until 100) { "Invalid success rate" }
    }

    fun worked(): Boolean = random.nextInt(100) <= successRate

    private companion object {
        val random = Random(1)
    }

}

class FlueShot : Treatment(50, 95)
class BoneMarrowTransplant(marrowCompatibility: Int) : Treatment(500, marrowCompatibility)
class BrainCirgury : Treatment(1000, 20)

