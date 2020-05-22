package pt.pakenuh.hollywood.sandbox.actor.proxy

import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.proxy.ActorProxyBase
import pt.pak3nuh.hollywood.actor.proxy.MsgParams
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
                param("pet", pet, false)
            }.build(ClinicActorProxySignatures.`checkinPet?pt_pakenuh_hollywood_sandbox_pet_Pet`)
        }
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        return sendAndAwait {
            parameters {
                param("petId", petId, false)
                param("creditCard", creditCard, false)
            }.build(ClinicActorProxySignatures.`checkoutPet?pt_pakenuh_hollywood_sandbox_pet_PetId&pt_pakenuh_hollywood_sandbox_owner_CreditCard`)
        }
    }

    override suspend fun petReady(pet: Pet) {
        return sendAndAwait {
            parameters {
                param("pet", pet, false)
            }.build(ClinicActorProxySignatures.`petReady?pt_pakenuh_hollywood_sandbox_pet_Pet`)
        }
    }

    override suspend fun orderExam(pet: Pet, exam: Exam): ExamResult {
        return sendAndAwait {
            parameters {
                param("pet", pet, false)
                param("exam", exam, false)
            }.build(ClinicActorProxySignatures.`orderExam?pt_pakenuh_hollywood_sandbox_pet_Pet&pt_pakenuh_hollywood_sandbox_clinic_Exam`)
        }
    }

    override suspend fun getPetToSee(petId: PetId): Pet {
        return sendAndAwait {
            parameters {
                param("petId", petId, false)
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

    override suspend fun onMessage(functionId: String, msgParams: MsgParams, unit: suspend (suspend () -> Unit) -> Response, value: suspend (suspend () -> Any?) -> Response, err: (String) -> Nothing): Response {
        return when (functionId) {
            ClinicActorProxySignatures.`checkinPet?pt_pakenuh_hollywood_sandbox_pet_Pet` -> unit { delegate.checkinPet(msgParams.getObject("pet")) }
            ClinicActorProxySignatures.`checkoutPet?pt_pakenuh_hollywood_sandbox_pet_PetId&pt_pakenuh_hollywood_sandbox_owner_CreditCard` -> value { delegate.checkoutPet(msgParams.getObject("petId"), msgParams.getObject("creditCard")) }
            ClinicActorProxySignatures.`petReady?pt_pakenuh_hollywood_sandbox_pet_Pet` -> unit { delegate.petReady(msgParams.getObject("pet")) }
            ClinicActorProxySignatures.`orderExam?pt_pakenuh_hollywood_sandbox_pet_Pet&pt_pakenuh_hollywood_sandbox_clinic_Exam` -> value { delegate.orderExam(msgParams.getObject("pet"), msgParams.getObject("exam")) }
            ClinicActorProxySignatures.`getPetToSee?pt_pakenuh_hollywood_sandbox_pet_PetId` -> value { delegate.getPetToSee(msgParams.getObject("petId")) }
            ClinicActorProxySignatures.`getPets?` -> value { delegate.getPets() }
            ClinicActorProxySignatures.`waitClosing?` -> unit { delegate.waitClosing() }
            else -> err("Function id $functionId unknown")
        }
    }
}

