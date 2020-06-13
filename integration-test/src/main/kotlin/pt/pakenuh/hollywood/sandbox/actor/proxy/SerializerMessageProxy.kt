package pt.pakenuh.hollywood.sandbox.actor.proxy

import pt.pak3nuh.hollywood.actor.proxy.ActorProxyBase
import pt.pak3nuh.hollywood.actor.proxy.HandlerBuilder
import pt.pak3nuh.hollywood.actor.proxy.MessageHandler
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pakenuh.hollywood.sandbox.actor.ClinicActor
import pt.pakenuh.hollywood.sandbox.actor.ClinicActorProxySignatures
import pt.pakenuh.hollywood.sandbox.clinic.Exam
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.Receipt
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId

open class ClinicBinaryProxy(delegate: ClinicActor, configuration: ProxyConfiguration) :
        ActorProxyBase<ClinicActor>(delegate, configuration), ClinicActor {

    override suspend fun checkinPet(pet: Pet) {
        return sendAndAwait {
            parameters {
                param("pet", Pet::class, pet)
            }.build(ClinicActorProxySignatures.`checkinPet?pt_pakenuh_hollywood_sandbox_pet_Pet`)
        }
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        return sendAndAwait {
            parameters {
                param("petId", PetId::class, petId)
                param("creditCard", CreditCard::class, creditCard)
            }.build(ClinicActorProxySignatures.`checkoutPet?pt_pakenuh_hollywood_sandbox_pet_PetId&pt_pakenuh_hollywood_sandbox_owner_CreditCard`)
        }
    }

    override suspend fun petReady(pet: Pet) {
        return sendAndAwait {
            parameters {
                param("pet", Pet::class, pet)
            }.build(ClinicActorProxySignatures.`petReady?pt_pakenuh_hollywood_sandbox_pet_Pet`)
        }
    }

    override suspend fun orderExam(pet: Pet, exam: Exam): ExamResult {
        return sendAndAwait {
            parameters {
                param("pet", Pet::class, pet)
                param("exam", Exam::class, exam)
            }.build(ClinicActorProxySignatures.`orderExam?pt_pakenuh_hollywood_sandbox_pet_Pet&pt_pakenuh_hollywood_sandbox_clinic_Exam`)
        }
    }

    override suspend fun getPetToSee(petId: PetId): Pet {
        return sendAndAwait {
            parameters {
                param("petId", PetId::class, petId)
            }.build(ClinicActorProxySignatures.`getPetToSee?pt_pakenuh_hollywood_sandbox_pet_PetId`)
        }
    }

    override suspend fun getPets(): List<PetId> {
        return sendAndAwait {
            build(ClinicActorProxySignatures.`getPets?`)
        }
    }

    override suspend fun waitClosing() {
        return sendAndAwait {
            build(ClinicActorProxySignatures.`waitClosing?`)
        }
    }

    override val handlerMap: Map<String, MessageHandler> = HandlerBuilder()
            .unitFunction(ClinicActorProxySignatures.`checkinPet?pt_pakenuh_hollywood_sandbox_pet_Pet`) { delegate.checkinPet(it.getObject("pet")) }
            .unitFunction(ClinicActorProxySignatures.`petReady?pt_pakenuh_hollywood_sandbox_pet_Pet`) { delegate.petReady(it.getObject("pet")) }
            .unitFunction(ClinicActorProxySignatures.`waitClosing?`) { delegate.waitClosing() }
            .valueFunction(ClinicActorProxySignatures.`checkoutPet?pt_pakenuh_hollywood_sandbox_pet_PetId&pt_pakenuh_hollywood_sandbox_owner_CreditCard`) { delegate.checkoutPet(it.getObject("petId"), it.getObject("creditCard")) }
            .valueFunction(ClinicActorProxySignatures.`orderExam?pt_pakenuh_hollywood_sandbox_pet_Pet&pt_pakenuh_hollywood_sandbox_clinic_Exam`) { delegate.orderExam(it.getObject("pet"), it.getObject("exam")) }
            .valueFunction(ClinicActorProxySignatures.`getPetToSee?pt_pakenuh_hollywood_sandbox_pet_PetId`) { delegate.getPetToSee(it.getObject("petId")) }
            .valueFunction(ClinicActorProxySignatures.`getPets?`) { delegate.getPets() }
            .build()

}
