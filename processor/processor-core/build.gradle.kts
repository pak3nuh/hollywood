import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    implementation(project(":api"))
    implementation(Dependencies.kotlinPoet)
    implementation(Dependencies.kotlinMetadata)
    testImplementation(Dependencies.mockk)
}
