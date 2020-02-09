package pt.pakenuh.hollywood.sandbox

import pt.pakenuh.hollywood.sandbox.clinic.ClinicFacade
import pt.pakenuh.hollywood.sandbox.owner.OwnerFacade
import pt.pakenuh.hollywood.sandbox.vet.VetFacade

interface PetClinic {
    val ownerFacade: OwnerFacade
    val vetFacade: VetFacade
    val clinicFacade: ClinicFacade
}
