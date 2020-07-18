package pt.pakenuh.hollywood.sandbox.pet

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import java.time.LocalDate

@Serializable
data class PetId(val registryId: String, val name: String, val ownerId: OwnerId, @ContextualSerialization val birthDate: LocalDate)
