package pt.pakenuh.hollywood.sandbox.pet

data class Pet(
        val petId: PetId,
        val brokenBones: Boolean = false,
        val skinRash: Boolean = false,
        val heartCondition: Boolean = false,
        val kidneyFailure: Boolean = false,
        val highBloodPressure: Boolean = false,
        val bloodToxins: Boolean = false
)
