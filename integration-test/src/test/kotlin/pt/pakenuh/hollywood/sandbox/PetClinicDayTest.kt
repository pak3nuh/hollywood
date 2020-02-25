package pt.pakenuh.hollywood.sandbox

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pt.pakenuh.hollywood.sandbox.actor.OwnerContacts
import pt.pakenuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pakenuh.hollywood.sandbox.clinic.PetClinic
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import java.time.LocalDate

class PetClinicDayTest {

    @Test
    fun petClinicDay() {
        val petClinic = createClinic()
        runBlocking {
            startDay(petClinic)
            petClinic.waitClosing()
            assertTrue(petClinic.currentPets().isEmpty())
        }
    }

    private suspend fun startDay(petClinic: PetClinic) {
        val johnId = OwnerId("123", "John")
        val mrBoots = Pet(
                PetId("12345678", "Mr Boots", johnId, LocalDate.of(2014, 1, 6)),
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
