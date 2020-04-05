import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    implementation(project(":api"))
    implementation(Dependencies.kotlinPoet)
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.1.0")
    testImplementation(Dependencies.mockk)
}
