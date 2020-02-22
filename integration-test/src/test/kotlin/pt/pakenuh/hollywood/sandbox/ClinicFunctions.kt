package pt.pakenuh.hollywood.sandbox

import pt.pak3nuh.hollywood.system.builder.SystemBuilder
import pt.pakenuh.hollywood.sandbox.actor.ClinicActors
import pt.pakenuh.hollywood.sandbox.actor.ClinicFactory
import pt.pakenuh.hollywood.sandbox.actor.OwnerFactory
import pt.pakenuh.hollywood.sandbox.actor.PetFactory
import pt.pakenuh.hollywood.sandbox.actor.VetFactory
import pt.pakenuh.hollywood.sandbox.clinic.PetClinic
import pt.pakenuh.hollywood.sandbox.clinic.PetClinicImpl
import pt.pakenuh.hollywood.sandbox.vet.Vet

fun createClinic(): PetClinic {

    val vets = listOf<Vet>(
            Vet("samir abdul")
    )

    val maxVetSlots = 5

    val actorSystem = SystemBuilder()
            .withProperty(ClinicActorsProperty) {
                ClinicActors(it.actorManager)
            }
            .registerFactory(ClinicFactory::class) { _, props ->
                ClinicFactory(vets, props[ClinicActorsProperty])
            }
            .registerFactory(OwnerFactory::class) { _, _ ->
                OwnerFactory()
            }
            .registerFactory(PetFactory::class) { _, props ->
                PetFactory(props[ClinicActorsProperty])
            }
            .registerFactory(VetFactory::class) { _, props ->
                VetFactory(props[ClinicActorsProperty], maxVetSlots)
            }
            .build()

    return PetClinicImpl(actorSystem, vets)
}

object ClinicActorsProperty : SystemBuilder.Property<ClinicActors>()
