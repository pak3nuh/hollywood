package pt.pakenuh.hollywood.sandbox.actor.proxy

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pt.pak3nuh.hollywood.actor.message.ExceptionReturn
import pt.pak3nuh.hollywood.actor.message.Message
import pt.pak3nuh.hollywood.actor.message.Parameter
import pt.pak3nuh.hollywood.actor.message.ReferenceParameter
import pt.pak3nuh.hollywood.actor.message.Response
import pt.pak3nuh.hollywood.actor.message.ReturnValue
import pt.pak3nuh.hollywood.actor.message.UnitReturn
import pt.pak3nuh.hollywood.actor.message.ValueReturn
import pt.pak3nuh.hollywood.actor.proxy.ActorProxyBase
import pt.pak3nuh.hollywood.actor.proxy.ProxyConfiguration
import pt.pakenuh.hollywood.sandbox.actor.ClinicActor
import pt.pakenuh.hollywood.sandbox.actor.OwnerContacts
import pt.pakenuh.hollywood.sandbox.clinic.Exam
import pt.pakenuh.hollywood.sandbox.clinic.ExamResult
import pt.pakenuh.hollywood.sandbox.clinic.Receipt
import pt.pakenuh.hollywood.sandbox.owner.CreditCard
import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import pt.pakenuh.hollywood.sandbox.pet.Pet
import pt.pakenuh.hollywood.sandbox.pet.PetId
import kotlin.coroutines.coroutineContext

open class ClinicBinaryProxy(delegate: ClinicActor, configuration: ProxyConfiguration) :
        ActorProxyBase<ClinicActor>(delegate, configuration), ClinicActor {

    private val serializer = configuration.serializer
    private val deserializer = configuration.deserializer
    private val createBuilder = configuration::newMessageBuilder

    override suspend fun checkinPet(pet: Pet, contacts: OwnerContacts) {
        // emmit msg builder
        val message: Message = createBuilder()
                // emmit parameters
                .parameters {
                    param("pet", pet, false)
                    param("contacts", contacts, false)
                }
                // emmit function build
                .build("checkinPet")
        // emmit dispatch call
        val returnValue = dispatch(message)
        return cast(returnValue)
    }

    override suspend fun checkoutPet(petId: PetId, creditCard: CreditCard): Receipt {
        val message: Message = createBuilder()
                .parameters {
                    param("petId", petId, false)
                    param("creditCard", creditCard, false)
                }.build("checkoutPet")
        return cast(dispatch(message))
    }

    override suspend fun petReady(pet: Pet) {
        val message: Message = createBuilder()
                .parameters {
                    param("pet", pet, false)
                }.build("petReady")
        return cast(dispatch(message))
    }

    override suspend fun orderExam(pet: Pet, exam: Exam): ExamResult {
        val message: Message = createBuilder()
                .parameters {
                    param("pet", pet, false)
                    param("exam", exam, false)
                }.build("orderExam")
        return cast(dispatch(message))
    }

    override suspend fun getPetToSee(petId: PetId): Pet {
        val message: Message = createBuilder()
                .parameters {
                    param("petId", petId, false)
                }.build("getPetToSee")
        return cast(dispatch(message))
    }

    override suspend fun getPets(): List<PetId> {
        val message: Message = createBuilder()
                .build("getPets")
        return cast(dispatch(message))
    }

    override suspend fun waitClosing() {
        val message: Message = createBuilder()
                .build("waitClosing")
        return cast(dispatch(message))
    }

    override suspend fun getOwnerContact(petId: PetId): OwnerContacts {
        val message: Message = createBuilder()
                .parameters {
                    param("petId", petId, false)
                }.build("getOwnerContact")
        return cast(dispatch(message))
    }

    override suspend fun getLatestOwnerContacts(ownerId: OwnerId): OwnerContacts {
        val message: Message = createBuilder()
                .parameters {
                    param("ownerId", ownerId, false)
                }.build("getLatestOwnerContacts")
        return cast(dispatch(message))
    }

    private suspend fun dispatch(message: Message): Any? {
        val packedMessage = serializer.serialize(message)
        val currentJob = requireNotNull(coroutineContext[Job])
        val result = CompletableDeferred<ByteArray>(parent = currentJob)

        // simulated mailbox
        // actual implementation should use the actorSystem to send and receive the results
        GlobalScope.launch {
            val response = try {
                onMessage(packedMessage)
            } catch (e: Exception) {
                ResponseImpl(ExceptionReturn(e))
            }
            result.complete(serializer.serialize(response))
        }

        return onResponse(result.await())
    }

    private fun onResponse(responseBytes: ByteArray): Any? {
        val response = deserializer.asResponse(responseBytes)
        return parse(response)
    }

    private fun parse(response: Response): Any? {
        return when (val returnValue = response.returnValue) {
            is ExceptionReturn -> throw returnValue.exception
            is ValueReturn -> returnValue.value
            UnitReturn -> Unit
        }
    }

    private suspend fun onMessage(packedMessage: ByteArray): Response {
        val message = deserializer.asMessage(packedMessage)
        val params = MsgParams(message)
        val functionResult = when (message.functionId) {
            "checkinPet:Lpt.pakenuh.hollywood.sandbox.pet.Pet;Lpt.pakenuh.hollywood.sandbox.actor.OwnerContacts" -> unitResult { delegate.checkinPet(params.getObject("pet"), params.getObject("contacts")) }
            "checkoutPet:Lpt.pakenuh.hollywood.sandbox.pet.PetId;Lpt.pakenuh.hollywood.sandbox.owner.CreditCard" -> valueResult { delegate.checkoutPet(params.getObject("petId"), params.getObject("creditCard")) }
            "petReady:Lpt.pakenuh.hollywood.sandbox.pet.Pet" -> unitResult { delegate.petReady(params.getObject("pet")) }
            "orderExam:Lpt.pakenuh.hollywood.sandbox.pet.Pet;Lpt.pakenuh.hollywood.sandbox.clinic.Exam" -> valueResult { delegate.orderExam(params.getObject("pet"), params.getObject("exam")) }
            "getPetToSee:Lpt.pakenuh.hollywood.sandbox.pet.PetId" -> valueResult { delegate.getPetToSee(params.getObject("petId")) }
            "getPets:" -> valueResult { delegate.getPets() }
            "waitClosing:" -> unitResult { delegate.waitClosing() }
            "getOwnerContact:Lpt.pakenuh.hollywood.sandbox.pet.PetId" -> valueResult { delegate.getOwnerContact(params.getObject("petId")) }
            "getLatestOwnerContacts:Lpt.pakenuh.hollywood.sandbox.owner.OwnerId" -> valueResult { delegate.getLatestOwnerContacts(params.getObject("ownerId")) }
            else -> error("Function id ${message.functionId} unknown")
        }
        return ResponseImpl(functionResult)
    }

    private inline fun <T> valueResult(block: () -> T): ReturnValue {
        return ValueReturn(block())
    }

    private inline fun unitResult(block: () -> Unit): ReturnValue {
        block()
        return UnitReturn
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

data class ResponseImpl(override val returnValue: ReturnValue): Response
