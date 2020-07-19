import pt.pak3nuh.hollywood.gradle.Dependencies

dependencies {
    implementation(project(":api"))
    implementation(project(":util"))
    implementation(project(":processor:processor-api"))
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.kotlinCoroutines)
    implementation(Dependencies.kryo)

    testImplementation("ch.qos.logback:logback-classic:1.2.3")
}
