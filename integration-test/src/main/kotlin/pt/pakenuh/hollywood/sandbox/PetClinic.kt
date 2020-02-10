package pt.pakenuh.hollywood.sandbox

import pt.pakenuh.hollywood.sandbox.clinic.ClinicFacade
import pt.pakenuh.hollywood.sandbox.owner.OwnerFacade

interface PetClinic {
    val ownerFacade: OwnerFacade
    val vetFacade: VetFacade
    val clinicFacade: ClinicFacade
}
