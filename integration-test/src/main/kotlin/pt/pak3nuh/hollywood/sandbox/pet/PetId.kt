package pt.pak3nuh.hollywood.sandbox.pet

import kotlinx.serialization.Serializable
import pt.pak3nuh.hollywood.sandbox.owner.OwnerId

@Serializable
data class PetId(val registryId: String, val name: String, val ownerId: OwnerId, val birthDate: String)
