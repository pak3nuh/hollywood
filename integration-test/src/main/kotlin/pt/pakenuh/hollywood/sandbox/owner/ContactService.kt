package pt.pakenuh.hollywood.sandbox.owner

import pt.pakenuh.hollywood.sandbox.actor.Treatment
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.OwnerContactResult

/**
 * Service to contact clients. Will contain callback functions (because its a test), then it needs to be a service and not an actor.
 */
class ContactService {

    private val contacts = mutableMapOf<OwnerId, OwnerContacts>()

    fun registerContact(ownerId: OwnerId, ownerContacts: OwnerContacts) {
        contacts[ownerId] = ownerContacts
    }

    fun updateContact(ownerId: OwnerId, result: ExamResult, treatment: Treatment): OwnerContactResult {
        return contacts.getValue(ownerId).updateContact(result, treatment);
    }

    suspend fun readyContact(ownerId: OwnerId) {
        contacts.getValue(ownerId).readyContact()
    }
}

/**
 * Contact data for a owner. In this test they are callback functions, so they can't be serialized.
 */
data class OwnerContacts(
        val updateContact: (ExamResult, Treatment) -> OwnerContactResult,
        val readyContact: suspend () -> Unit
)
