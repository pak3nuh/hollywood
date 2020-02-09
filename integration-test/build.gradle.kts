import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    implementation(project(":api"))
    implementation(Dependencies.kotlinCoroutines)
    testImplementation(project(":builder"))
}
