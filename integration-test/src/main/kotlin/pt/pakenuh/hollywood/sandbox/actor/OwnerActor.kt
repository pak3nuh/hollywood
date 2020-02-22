package pt.pakenuh.hollywood.sandbox.actor

import pt.pak3nuh.hollywood.actor.proxy.ActorProxy
import pt.pak3nuh.hollywood.processor.Actor
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.OwnerContactResult

typealias OwnerContactFunction = suspend (result: ExamResult, treatment: Treatment) -> OwnerContactResult

@Actor
interface OwnerActor {
    suspend fun contact(result: ExamResult, treatment: Treatment): OwnerContactResult
    suspend fun petReady()
}

class OwnerProxy(override val delegate: OwnerActor, override val actorId: String) : ActorProxy<OwnerActor>, OwnerActor by delegate

class OwnerFactory : FactoryBase<OwnerActor, OwnerProxy>(OwnerActor::class, OwnerProxy::class, ::OwnerProxy) {
    fun createOwner(ownerContacts: OwnerContacts): OwnerActor = OwnerActorImpl(ownerContacts)
}

data class OwnerContacts(
        val updateContact: (ExamResult, Treatment) -> OwnerContactResult,
        val readyContact: suspend () -> Unit
)

private class OwnerActorImpl(private val ownerContacts: OwnerContacts) : OwnerActor {

    override suspend fun contact(result: ExamResult, treatment: Treatment): OwnerContactResult {
        return ownerContacts.updateContact(result, treatment)
    }

    override suspend fun petReady() {
        ownerContacts.readyContact()
    }

}
