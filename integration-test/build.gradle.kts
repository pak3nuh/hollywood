import pt.pak3nuh.hollywood.gradle.Dependencies

plugins {
    kotlin("kapt")
}

dependencies {
    kapt(project(":processor:processor-core"))
    implementation(project(":api"))
    implementation(Dependencies.kotlinCoroutines)
    testImplementation(project(":builder"))
}
