package pt.pakenuh.hollywood.sandbox.pet

import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import java.time.LocalDate

data class PetId(val registryId: String, val name: String, val ownerId: OwnerId, val birthDate: LocalDate)
