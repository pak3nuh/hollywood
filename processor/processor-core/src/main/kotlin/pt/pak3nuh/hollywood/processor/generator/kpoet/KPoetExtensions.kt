package pt.pak3nuh.hollywood.processor.generator.kpoet

import com.squareup.kotlinpoet.TypeName

private val arrayRegex = Regex("^kotlin\\.(.*)Array")

fun TypeName.isArray() = this.toString().contains(arrayRegex)
