import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    implementation(project(":api"))
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.kotlinCoroutines)
}
