package pt.pakenuh.hollywood.sandbox.pet

import kotlinx.coroutines.delay
import pt.pakenuh.hollywood.sandbox.actor.Treatment

class Pet(
        val petId: PetId,
        var brokenBones: Boolean = false,
        var hasSkinRash: Boolean = false,
        var getsTiredFast: Boolean = false,
        var hasFainted: Boolean = false,
        var peesBlood: Boolean = false
) {
    suspend fun applyTreatment(treatment: Treatment) {
        // no animals die here
        when (treatment) {
            Treatment.APPLY_CAST -> brokenBones = false
            Treatment.HEART_CIRGURY -> getsTiredFast = false
            Treatment.BLOOD_THINNER -> hasFainted = false
            Treatment.CORTIZONE -> hasSkinRash = false
            Treatment.KIDNEY_TRANSPLANT -> peesBlood = false
        }
        // treatment to take effect
        delay(treatment.timeInSec.toLong() * 1000)
    }
}
