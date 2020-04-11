package pt.pak3nuh.hollywood.gradle

private object Versions {
    // plugin versions must be changed inline
    val kotlin = "1.3.70"
    val junit = "5.6.0"
    val assertK = "0.20"
    val coroutines = "1.3.4"
    val mockk = "1.9.3"
    val kryo = "5.0.0-RC5"
}

object Dependencies {
    val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"
    val kotlinCoroutines = kotlinx("kotlinx-coroutines-core", Versions.coroutines)
    val kotlinMetadata = kotlinx("kotlinx-metadata-jvm", "0.1.0")

    // todo as of kotiln 1.3.70 this may not be needed
    val kotlinReflect = kotlin("reflect", Versions.kotlin)
    val kotlinStdLib = kotlin("stdlib-jdk8", Versions.kotlin)
    val junitApi = junitJupiter("api", Versions.junit)
    val junitEngine = junitJupiter("engine", Versions.junit)
    val assertK = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertK}"
    val mockk = "io.mockk:mockk:${Versions.mockk}"
    val kryo = "com.esotericsoftware:kryo:${Versions.kryo}"
}

private fun kotlinx(name: String, version: String) = "org.jetbrains.kotlinx:$name:$version"
private fun kotlin(name: String, version: String) = "org.jetbrains.kotlin:kotlin-$name:$version"
private fun junitJupiter(name: String, version: String) = "org.junit.jupiter:junit-jupiter-$name:$version"
