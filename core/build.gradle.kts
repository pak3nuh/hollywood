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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:0.20.0")

    testImplementation("ch.qos.logback:logback-classic:1.2.3")
}
