package pt.pak3nuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.processor.Actor
import pt.pak3nuh.hollywood.sandbox.clinic.ExamResult
import pt.pak3nuh.hollywood.sandbox.clinic.OwnerContactResult
import pt.pak3nuh.hollywood.sandbox.owner.ContactService
import pt.pak3nuh.hollywood.sandbox.owner.OwnerId

@Actor
interface OwnerActor {
    suspend fun contact(result: ExamResult, treatment: Treatment): OwnerContactResult
    suspend fun petReady()
}

interface OwnerFactory: OwnerActorBaseFactory {
    fun createOwner(ownerId: OwnerId): OwnerActor
}

class OwnerFactoryImpl(private val contactService: ContactService) : OwnerFactory {
    override fun createOwner(ownerId: OwnerId): OwnerActor = OwnerActorImpl(ownerId, contactService)
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
