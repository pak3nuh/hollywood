import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    implementation(project(":processor:processor-api"))
    implementation(Dependencies.kotlinPoet)
}
