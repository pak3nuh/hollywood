package pt.pak3nuh.hollywood.sandbox

import kotlinx.coroutines.Job
import pt.pak3nuh.hollywood.sandbox.actor.ClinicActors
import pt.pak3nuh.hollywood.sandbox.actor.ClinicFactory
import pt.pak3nuh.hollywood.sandbox.actor.OwnerFactory
import pt.pak3nuh.hollywood.sandbox.actor.OwnerFactoryImpl
import pt.pak3nuh.hollywood.sandbox.actor.PetFactory
import pt.pak3nuh.hollywood.sandbox.actor.VetFactory
import pt.pak3nuh.hollywood.sandbox.clinic.PetClinic
import pt.pak3nuh.hollywood.sandbox.clinic.PetClinicImpl
import pt.pak3nuh.hollywood.sandbox.owner.ContactService
import pt.pak3nuh.hollywood.sandbox.vet.Vet
import pt.pak3nuh.hollywood.system.builder.SystemBuilder

fun createClinic(): PetClinic {

    val vets = listOf<Vet>(
            Vet("samir abdul")
    )

    val maxVetSlots = 5

    val contactService = ContactService()
    val actorSystem = SystemBuilder()
            .withProperty(ClinicActorsProperty) { ClinicActors(it.actorManager) }
            // fail fast job
            .withProperty(MainJob) { Job(it.actorScope.mainJob) }
            .registerFactory(ClinicFactory::class) { _, props ->
                ClinicFactory(vets, props[ClinicActorsProperty], props[MainJob])
            }
            .registerFactory(OwnerFactory::class) { _, _ ->
                OwnerFactoryImpl(contactService)
            }
            .registerFactory(PetFactory::class) { _, props ->
                PetFactory(props[ClinicActorsProperty])
            }
            .registerFactory(VetFactory::class) { _, props ->
                VetFactory(props[ClinicActorsProperty], maxVetSlots)
            }
            .build()

    return PetClinicImpl(actorSystem, vets, contactService)
}

object ClinicActorsProperty : SystemBuilder.Property<ClinicActors>()
object MainJob: SystemBuilder.Property<Job>()
