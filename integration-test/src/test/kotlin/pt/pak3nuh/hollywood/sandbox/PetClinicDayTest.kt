package pt.pak3nuh.hollywood.sandbox

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import pt.pak3nuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pak3nuh.hollywood.sandbox.clinic.PetClinic
import pt.pak3nuh.hollywood.sandbox.owner.CreditCard
import pt.pak3nuh.hollywood.sandbox.owner.OwnerContacts
import pt.pak3nuh.hollywood.sandbox.owner.OwnerId
import pt.pak3nuh.hollywood.sandbox.pet.Pet
import pt.pak3nuh.hollywood.sandbox.pet.PetId

class PetClinicDayTest {

    @Disabled // test doesn't complete because actors are deadlocked, need to make a propper test
    @Test
    fun petClinicDay() {
        val petClinic = createClinic()
        runBlocking(context = petClinic.actorScope.coroutineContext) {
            startDay(petClinic)
            petClinic.waitClosing()
            assertTrue(petClinic.currentPets().isEmpty())
        }
    }

    private suspend fun startDay(petClinic: PetClinic) {
        val johnId = OwnerId("123", "John")
        val mrBoots = Pet(
                PetId("12345678", "Mr Boots", johnId, "2014-01-06"),
                brokenBones = true
        )
        val johnCreditCard = CreditCard("1", 5000)
        petClinic.checkinPet(mrBoots, OwnerContacts({ _, _ ->
            OwnerContactResult.APPLY_TREATMENT
        }, {
            petClinic.checkoutPet(mrBoots.petId, johnCreditCard)
        }))
    }
}
