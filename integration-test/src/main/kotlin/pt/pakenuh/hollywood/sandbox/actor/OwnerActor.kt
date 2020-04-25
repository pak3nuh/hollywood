package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pakenuh.hollywood.sandbox.owner.ContactService
import pt.pakenuh.hollywood.sandbox.owner.OwnerId

@Actor
interface OwnerActor {
    suspend fun contact(result: ExamResult, treatment: Treatment): OwnerContactResult
    suspend fun petReady()
}

class OwnerFactory(private val contactService: ContactService) : OwnerActorBaseFactory {
    fun createOwner(ownerId: OwnerId): OwnerActor = OwnerActorImpl(ownerId, contactService)
}

private class OwnerActorImpl(
        private val ownerId: OwnerId,
        private val contactService: ContactService
) : OwnerActor {

    override suspend fun contact(result: ExamResult, treatment: Treatment): OwnerContactResult {
        return contactService.updateContact(ownerId, result, treatment)
    }

    override suspend fun petReady() {
        contactService.readyContact(ownerId)
    }

}
