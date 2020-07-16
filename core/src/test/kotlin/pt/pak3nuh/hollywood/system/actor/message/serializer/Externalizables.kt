package pt.pak3nuh.hollywood.system.actor.message.serializer

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

class ExternalizableString(): Externalizable {

    lateinit var value: String

    constructor(value: String): this() {
        this.value = value
    }

    override fun readExternal(input: ObjectInput) {
        value = input.readUTF()
    }

    override fun writeExternal(out: ObjectOutput) {
        out.writeUTF(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExternalizableString

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

}

class RecursiveExternalizable(): Externalizable {

    lateinit var value: Externalizable
    lateinit var kclass: String

    constructor(value: Externalizable): this() {
        this.value = value
        this.kclass = value::class.qualifiedName!!
    }

    override fun readExternal(input: ObjectInput) {
        kclass = input.readUTF()
        value = Class.forName(kclass).newInstance() as Externalizable
        value.readExternal(input)
    }

    override fun writeExternal(out: ObjectOutput) {
        out.writeUTF(kclass)
        value.writeExternal(out)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecursiveExternalizable

        if (value != other.value) return false
        if (kclass != other.kclass) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + kclass.hashCode()
        return result
    }


}