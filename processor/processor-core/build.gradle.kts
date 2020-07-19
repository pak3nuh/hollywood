import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    implementation(project(":api"))
    implementation(project(":processor:processor-api"))
    implementation(Dependencies.kotlinPoet)
    implementation(Dependencies.kotlinMetadata)
    implementation(Dependencies.kotlinSerializationRuntime)
    testImplementation(Dependencies.mockk)
}
