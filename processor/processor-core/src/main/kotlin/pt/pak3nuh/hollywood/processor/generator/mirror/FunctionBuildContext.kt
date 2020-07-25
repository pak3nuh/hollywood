package pt.pak3nuh.hollywood.processor.generator.mirror

import com.squareup.kotlinpoet.ClassName
import pt.pak3nuh.hollywood.processor.generator.context.Property

data class FunctionBuildContext(
        val signatureType: ClassName
) {

    companion object Key: Property<FunctionBuildContext>

}
