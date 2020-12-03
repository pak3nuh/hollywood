package pt.pak3nuh.hollywood.sandbox

import pt.pak3nuh.hollywood.sandbox.actor.ClinicActors
import pt.pak3nuh.hollywood.sandbox.actor.ClinicFactory
import pt.pak3nuh.hollywood.sandbox.actor.OwnerFactory
import pt.pak3nuh.hollywood.sandbox.actor.OwnerFactoryImpl
import pt.pak3nuh.hollywood.sandbox.actor.PetFactory
import pt.pak3nuh.hollywood.sandbox.actor.VetFactory
import pt.pak3nuh.hollywood.sandbox.clinic.PetClinicFacade
import pt.pak3nuh.hollywood.sandbox.owner.ContactService
import pt.pak3nuh.hollywood.sandbox.vet.Vet
import pt.pak3nuh.hollywood.system.builder.SystemBuilder

fun createClinic(): PetClinicFacade {

    val maxVetSlots = 5

    val contactService = ContactService()
    val actorSystem = SystemBuilder()
            .withProperty(ClinicActorsProperty) { ClinicActors(it.actorManager) }
            .registerFactory(ClinicFactory::class) { system, props ->
                ClinicFactory(props[ClinicActorsProperty], system.actorScope)
            }
            .registerFactory(OwnerFactory::class) { _, _ ->
                OwnerFactoryImpl(contactService)
            }
            .registerFactory(PetFactory::class) { _, props ->
                PetFactory(props[ClinicActorsProperty])
            }
            .registerFactory(VetFactory::class) { system, props ->
                VetFactory(props[ClinicActorsProperty], maxVetSlots, system.actorScope)
            }
            .build()

    return PetClinicFacade(actorSystem, contactService)
}

object ClinicActorsProperty : SystemBuilder.Property<ClinicActors>()
