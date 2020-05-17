package pt.pakenuh.hollywood.sandbox.actor.proxy

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pt.pak3nuh.hollywood.actor.message.ExceptionResponse
import pt.pak3nuh.hollywood.actor.message.ExceptionReturn
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.ReturnType
import pt.pak3nuh.hollywood.actor.message.UnitResponse
import pt.pak3nuh.hollywood.actor.message.ValueResponse
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import pt.pak3nuh.hollywood.actor.proxy.ActorProxyBase
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pakenuh.hollywood.sandbox.actor.ClinicActor
import pt.pakenuh.hollywood.sandbox.actor.ClinicActorProxySignatures
import pt.pakenuh.hollywood.sandbox.clinic.Exam
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.Receipt
import pt.pakenuh.hollywood.sandbox.coroutine.TestScope
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import kotlin.coroutines.coroutineContext

open class ClinicBinaryProxy(delegate: ClinicActor, configuration: ProxyConfiguration) :
        ActorProxyBase<ClinicActor>(delegate, configuration), ClinicActor {

    private val serializer = configuration.serializer
    private val deserializer = configuration.deserializer
    private val createBuilder = configuration::newMessageBuilder

    override suspend fun checkinPet(pet: Pet) {
        // emmit msg builder
        val message: Message = createBuilder()
                // emmit parameters
                .parameters {
                    param("pet", pet, false)
                }
                // emmit function build
                .build(ClinicActorProxySignatures.`checkinPet?pt_pakenuh_hollywood_sandbox_pet_Pet`)
        // emmit dispatch call
        val returnValue = dispatch(message)
        return cast(returnValue)
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        val message: Message = createBuilder()
                .parameters {
                    param("petId", petId, false)
                    param("creditCard", creditCard, false)
                }.build(ClinicActorProxySignatures.`checkoutPet?pt_pakenuh_hollywood_sandbox_pet_PetId&pt_pakenuh_hollywood_sandbox_owner_CreditCard`)
        return cast(dispatch(message))
    }

    override suspend fun petReady(pet: Pet) {
        val message: Message = createBuilder()
                .parameters {
                    param("pet", pet, false)
                }.build(ClinicActorProxySignatures.`petReady?pt_pakenuh_hollywood_sandbox_pet_Pet`)
        return cast(dispatch(message))
    }

    override suspend fun orderExam(pet: Pet, exam: Exam): ExamResult {
        val message: Message = createBuilder()
                .parameters {
                    param("pet", pet, false)
                    param("exam", exam, false)
                }.build(ClinicActorProxySignatures.`orderExam?pt_pakenuh_hollywood_sandbox_pet_Pet&pt_pakenuh_hollywood_sandbox_clinic_Exam`)
        return cast(dispatch(message))
    }

    override suspend fun getPetToSee(petId: PetId): Pet {
        val message: Message = createBuilder()
                .parameters {
                    param("petId", petId, false)
                }.build(ClinicActorProxySignatures.`getPetToSee?pt_pakenuh_hollywood_sandbox_pet_PetId`)
        return cast(dispatch(message))
    }

    override suspend fun getPets(): List<PetId> {
        val message: Message = createBuilder()
                .build(ClinicActorProxySignatures.`getPets?`)
        return cast(dispatch(message))
    }

    override suspend fun waitClosing() {
        val message: Message = createBuilder()
                .build(ClinicActorProxySignatures.`waitClosing?`)
        return cast(dispatch(message))
    }

    private suspend fun dispatch(message: Message): Any? {
        val packedMessage = serializer.serialize(message)
        val currentJob = requireNotNull(coroutineContext[Job])
        val result = CompletableDeferred<ByteArray>(parent = currentJob)

        // simulated mailbox
        // actual implementation should use the actorSystem to send and receive the results
        TestScope.launch {
            val response = try {
                onMessage(packedMessage)
            } catch (e: Exception) {
                ExceptionResponse(e)
            }
            val asBytes = serializer.serialize(response)
            result.complete(asBytes)
        }

        val responseBytes = result.await()
        return onResponse(responseBytes)
    }

    private fun onResponse(responseBytes: ByteArray): Any? {
        val response = deserializer.asResponse(responseBytes)
        return parse(response)
    }

    private fun parse(response: Response): Any? {
        return when (response.returnType) {
            ReturnType.EXCEPTION -> {
                val exceptionReturn = response.returnValue as ExceptionReturn
                throw ProxyResponseException(exceptionReturn.message, exceptionReturn.stackTrace)
            }
            ReturnType.VALUE -> (response.returnValue as ValueReturn).value
            ReturnType.UNIT -> Unit
        }
    }

    private suspend fun onMessage(packedMessage: ByteArray): Response {
        val message = deserializer.asMessage(packedMessage)
        val params = MsgParams(message)
        return when (message.functionId) {
            ClinicActorProxySignatures.`checkinPet?pt_pakenuh_hollywood_sandbox_pet_Pet` -> unitResult { delegate.checkinPet(params.getObject("pet")) }
            ClinicActorProxySignatures.`checkoutPet?pt_pakenuh_hollywood_sandbox_pet_PetId&pt_pakenuh_hollywood_sandbox_owner_CreditCard` -> valueResult { delegate.checkoutPet(params.getObject("petId"), params.getObject("creditCard")) }
            ClinicActorProxySignatures.`petReady?pt_pakenuh_hollywood_sandbox_pet_Pet` -> unitResult { delegate.petReady(params.getObject("pet")) }
            ClinicActorProxySignatures.`orderExam?pt_pakenuh_hollywood_sandbox_pet_Pet&pt_pakenuh_hollywood_sandbox_clinic_Exam` -> valueResult { delegate.orderExam(params.getObject("pet"), params.getObject("exam")) }
            ClinicActorProxySignatures.`getPetToSee?pt_pakenuh_hollywood_sandbox_pet_PetId` -> valueResult { delegate.getPetToSee(params.getObject("petId")) }
            ClinicActorProxySignatures.`getPets?` -> valueResult { delegate.getPets() }
            ClinicActorProxySignatures.`waitClosing?` -> unitResult { delegate.waitClosing() }
            else -> error("Function id ${message.functionId} unknown")
        }
    }

    private inline fun <T> valueResult(block: () -> T): Response {
        val value = block()
        return ValueResponse(value)
    }

    private inline fun unitResult(block: () -> Unit): Response {
        block()
        return UnitResponse()
    }

    private fun <T> cast(value: Any?): T = value as T
}

class MsgParams(private val message: Message) {

    fun <T> getObject(name: String): T = getObjectNullable(name) ?: error("Parameter $name can't be null")

    @Suppress("UNCHECKED_CAST")
    fun <T> getObjectNullable(name: String): T? {
        val param = getParam(name)
        return (param as ReferenceParameter).value as T?
    }

    private fun getParam(name: String): Parameter {
        val nullParam = message.parameters.firstOrNull { it.name == name }
        return requireNotNull(nullParam) { "Parameter with name $name not found" }
    }
}

class ProxyResponseException(message: String?, stackTrace: List<StackTraceElement>?) : RuntimeException(message)
