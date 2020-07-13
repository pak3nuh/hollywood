package pt.pak3nuh.hollywood.gradle

private object Versions {
    // plugin versions must be changed inline
    const val kotlin = "1.3.72"
    const val coroutines = "1.3.5"
    const val metadata = "0.1.0"
    const val junit = "5.6.0"
    const val kotlinPoet = "1.5.0"
    const val assertK = "0.20"
    const val mockk = "1.9.3"
    const val kryo = "5.0.0-RC5"
    const val sfl4j = "1.7.30"
}

object Dependencies {
    val sfl4j = artifact("org.slf4j", "slf4j-api", Versions.sfl4j)
    val kotlinPoet = artifact("com.squareup", "kotlinpoet", Versions.kotlinPoet)
    val kotlinCoroutines = kotlinx("kotlinx-coroutines-core", Versions.coroutines)
    val kotlinMetadata = kotlinx("kotlinx-metadata-jvm", Versions.metadata)

    // todo as of kotiln 1.3.70 this may not be needed
    val kotlinReflect = kotlin("reflect", Versions.kotlin)
    val kotlinStdLib = kotlin("stdlib-jdk8", Versions.kotlin)
    val junitApi = junitJupiter("api", Versions.junit)
    val junitEngine = junitJupiter("engine", Versions.junit)
    val assertK = artifact("com.willowtreeapps.assertk", "assertk-jvm", Versions.assertK)
    val mockk = artifact("io.mockk", "mockk", Versions.mockk)
    val kryo = artifact("com.esotericsoftware", "kryo", Versions.kryo)
}

private fun artifact(group: String, name: String, version: String) = "$group:$name:$version"
private fun kotlinx(name: String, version: String) = artifact("org.jetbrains.kotlinx", name, version)
private fun kotlin(name: String, version: String) = artifact("org.jetbrains.kotlin", "kotlin-$name", version)
private fun junitJupiter(name: String, version: String) = artifact("org.junit.jupiter", "junit-jupiter-$name", version)
