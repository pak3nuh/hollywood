package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pakenuh.hollywood.sandbox.owner.OwnerId

@Actor
interface OwnerActor {
    suspend fun contact(result: ExamResult, treatment: Treatment): OwnerContactResult
    suspend fun petReady()
}

class OwnerFactory(private val clinicActors: ClinicActors) : OwnerActorBaseFactory {
    fun createOwner(ownerId: OwnerId): OwnerActor = OwnerActorImpl(clinicActors, ownerId)
}

data class OwnerContacts(
        val updateContact: (ExamResult, Treatment) -> OwnerContactResult,
        // todo this breaks the requirements of the system, actors shouldn't communicate directly
        // also breaks serializer
        val readyContact: suspend () -> Unit
)

private class OwnerActorImpl(clinicActors: ClinicActors, private val ownerId: OwnerId) : OwnerActor {

    private val clinicActor = clinicActors.getClinic()
    private var contacts: OwnerContacts? = null

    override suspend fun contact(result: ExamResult, treatment: Treatment): OwnerContactResult {
        return getOwnerContacts().updateContact(result, treatment)
    }

    override suspend fun petReady() {
        getOwnerContacts().readyContact()
    }

    private suspend fun getOwnerContacts(): OwnerContacts {
        if (contacts == null) {
            contacts = clinicActor.getLatestOwnerContacts(ownerId)
        }
        return contacts!!
    }

}
