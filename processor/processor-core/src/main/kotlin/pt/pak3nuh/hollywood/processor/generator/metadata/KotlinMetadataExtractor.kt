@file:Suppress("UNCHECKED_CAST")

package pt.pak3nuh.hollywood.processor.generator.metadata

import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import pt.pak3nuh.hollywood.processor.generator.metadata.type.MetaClass
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement

class KotlinMetadataExtractor(
        private val metadataType: TypeElement
) {

    fun extract(typeElement: TypeElement): MetaClass? {
        val metadataAnnotation = typeElement.annotationMirrors.first { it.annotationType.asElement() == metadataType }
        val header = convertHeader(metadataAnnotation)
        val metadata = KotlinClassMetadata.read(header)
        return if (metadata == null) {
            null
        } else {
            MetaClass((metadata as KotlinClassMetadata.Class).toKmClass())
        }
    }

    private fun convertHeader(metadataAnnotation: AnnotationMirror): KotlinClassHeader {
        return KotlinClassHeader(
                getKind(metadataAnnotation),
                getMetadataVersion(metadataAnnotation),
                getBytecodeVersion(metadataAnnotation),
                getData1(metadataAnnotation),
                getData2(metadataAnnotation),
                getExtraString(metadataAnnotation),
                getPackageName(metadataAnnotation),
                getExtraInt(metadataAnnotation)
        )
    }

    private fun getPackageName(metadataAnnotation: AnnotationMirror): String? {
        return getData(metadataAnnotation, "pn")?.toString()
    }

    private fun getExtraString(metadataAnnotation: AnnotationMirror): String? {
        return getData(metadataAnnotation, "xs")?.toString()
    }

    private fun getData2(metadataAnnotation: AnnotationMirror): Array<String>? {
        val data = getData(metadataAnnotation, "d2")
        return asStringArray(data)
    }

    private fun getData1(metadataAnnotation: AnnotationMirror): Array<String>? {
        val data = getData(metadataAnnotation, "d1")
        return asStringArray(data)
    }

    private fun getBytecodeVersion(metadataAnnotation: AnnotationMirror): IntArray? {
        val data = getData(metadataAnnotation, "bv")
        return asIntArray(data)
    }

    private fun getMetadataVersion(metadataAnnotation: AnnotationMirror): IntArray? {
        val data = getData(metadataAnnotation, "mv")
        return asIntArray(data)
    }

    private fun asStringArray(data: Any?): Array<String>? {
        val list = data as List<AnnotationValue>?
        return list?.map { it.value as String }?.toTypedArray()
    }

    private fun asIntArray(data: Any?): IntArray? {
        val list = data as List<AnnotationValue>?
        return list?.map { it.value as Int }?.toIntArray()
    }

    private fun getExtraInt(metadataAnnotation: AnnotationMirror): Int? {
        return getData(metadataAnnotation, "xi") as Int?
    }

    private fun getKind(metadataAnnotation: AnnotationMirror): Int? {
        return getData(metadataAnnotation, "k") as Int?
    }

    /**
     * Obtains the value of the annotation member. The actual type is not what is on the source code,
     * but a mirror based projection over it. Details on [AnnotationValue].
     */
    private fun getData(metadataAnnotation: AnnotationMirror, name: String): Any? {
        val annotationValue = metadataAnnotation.elementValues
                .asSequence()
                .firstOrNull { it.key.simpleName.toString() == name }
                ?.value

        return annotationValue?.value
    }

}
