import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    api(project(":api"))
    implementation(project(":core"))
    implementation(Dependencies.kotlinReflect)
}
