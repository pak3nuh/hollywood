import pt.pak3nuh.hollywood.gradle.Versions

dependencies {
    implementation(project(":api"))
    implementation(kotlin("reflect", Versions.kotlin))
}
