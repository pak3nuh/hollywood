import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    api(Dependencies.kotlinCoroutines)
    api(project(":processor:processor-api"))
    implementation(Dependencies.sfl4j)
}
