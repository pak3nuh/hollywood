import pt.pak3nuh.hollywood.gradle.Dependencies

plugins {
    kotlin("kapt")
    kotlin("plugin.serialization") version pt.pak3nuh.hollywood.gradle.Versions.kotlin
}

dependencies {
    kapt(project(":processor:processor-core"))
    implementation(project(":api"))
    implementation(project(":processor:processor-api"))
    implementation(Dependencies.kotlinCoroutines)
    implementation(Dependencies.kotlinSerializationRuntime)
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:0.20.0")
    testImplementation(project(":builder"))
}
