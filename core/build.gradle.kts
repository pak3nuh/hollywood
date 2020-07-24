import pt.pak3nuh.hollywood.gradle.Dependencies

plugins {
    kotlin("plugin.serialization") version pt.pak3nuh.hollywood.gradle.Versions.kotlin
}

dependencies {
    implementation(project(":api"))
    implementation(project(":util"))
    implementation(project(":processor:processor-api"))
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.kotlinCoroutines)
    implementation(Dependencies.kryo)
    implementation(Dependencies.kotlinSerializationCbor)

    testImplementation(Dependencies.logback)
}
