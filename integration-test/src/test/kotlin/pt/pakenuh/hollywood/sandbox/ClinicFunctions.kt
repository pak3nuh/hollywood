package pt.pakenuh.hollywood.sandbox

import pt.pak3nuh.hollywood.system.builder.SystemBuilder
import pt.pakenuh.hollywood.sandbox.actor.ClinicFactory

fun createClinic(): PetClinic {

    val actorSystem = SystemBuilder()
            .registerFactory(ClinicFactory())
            .build()

    return PetClinicImpl(actorSystem)
}
