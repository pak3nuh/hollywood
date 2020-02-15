package pt.pakenuh.hollywood.sandbox

import pt.pak3nuh.hollywood.system.builder.SystemBuilder
import pt.pakenuh.hollywood.sandbox.actor.ClinicFactory
import pt.pakenuh.hollywood.sandbox.actor.OwnerFactory
import pt.pakenuh.hollywood.sandbox.actor.PetFactory
import pt.pakenuh.hollywood.sandbox.actor.VetFactory

fun createClinic(): PetClinic {

    // todo cyclic dependency!
    val actors

    val actorSystem = SystemBuilder()
            .registerFactory(ClinicFactory::class, ClinicFactory())
            .registerFactory(OwnerFactory::class, OwnerFactory())
            .registerFactory(PetFactory::class, PetFactory())
            .registerFactory(VetFactory::class, VetFactory())
            .build()

    return PetClinicImpl(actorSystem)
}
