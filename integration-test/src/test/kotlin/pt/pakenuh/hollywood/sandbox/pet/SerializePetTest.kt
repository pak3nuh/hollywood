package pt.pakenuh.hollywood.sandbox.pet

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotSameAs
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Test
import pt.pakenuh.hollywood.sandbox.owner.OwnerId
import java.time.LocalDate

internal class SerializePetTest {
    @Test
    internal fun `should serdes pet instances`() {
        val pet = PetId("123-asd", "Mr fluffy boots", OwnerId("asdewq", "Travolta"), LocalDate.now())
        val format = Cbor(context = SerializersModule {
            contextual(LocalDate::class, LocalDateSerializer())
        })
        val dump = format.dump(PetId.serializer(), pet)
        val clone = format.load(PetId.serializer(), dump)
        assertThat(pet).all {
            isEqualTo(clone)
            isNotSameAs(clone)
        }
    }
}

internal class LocalDateSerializer : KSerializer<LocalDate> {

    override val descriptor: SerialDescriptor = SerialDescriptor("LocalDate") {
        element("epochDay", Long.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val epochDay = decoder.decodeLong()
        return LocalDate.ofEpochDay(epochDay)
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeLong(value.toEpochDay())
    }
}
