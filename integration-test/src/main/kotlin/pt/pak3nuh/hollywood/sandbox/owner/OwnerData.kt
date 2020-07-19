package pt.pak3nuh.hollywood.sandbox.owner

import kotlinx.serialization.Serializable
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

@Serializable
data class OwnerId(val id: String, val name: String)

private interface CreditCardApi {
    val number: String
    val plafond: Int
}

data class CreditCard private constructor(private val data: Data) : Externalizable by data, CreditCardApi by data {

    // inner data class for var data fields
    private data class Data(override var number: String, override var plafond: Int) : Externalizable, CreditCardApi {
        override fun readExternal(input: ObjectInput) {
            number = input.readUTF()
            plafond = input.readInt()
        }

        override fun writeExternal(output: ObjectOutput) {
            output.writeUTF(number)
            output.writeInt(plafond)
        }
    }

    constructor() : this(Data("", 0))
    constructor(number: String, plafond: Int) : this(Data(number, plafond))

}
