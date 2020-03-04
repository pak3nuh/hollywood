package pt.pak3nuh.hollywood.gradle

object Versions {
    // plugin versions must be changed inline
    val kotlin = "1.3.61"
    val junit = "5.6.0"
    val assertK = "0.20"
    val coroutines = "1.3.3"
}

object Dependencies {
    val kotlinPoet = "com.squareup:kotlinpoet:1.5.0"
    val kotlinCoroutines = kotlinx("kotlinx-coroutines-core", Versions.coroutines)
    val kotlinReflect = kotlin("reflect", Versions.kotlin)
    val kotlinStdLib = kotlin("stdlib-jdk8", Versions.kotlin)
    val junitApi = junitJupiter("api", Versions.junit)
    val junitEngine = junitJupiter("engine", Versions.junit)
    val assertK = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertK}"
}

private fun kotlinx(name: String, version: String) = "org.jetbrains.kotlinx:$name:$version"
private fun kotlin(name: String, version: String) = "org.jetbrains.kotlin:kotlin-$name:$version"
private fun junitJupiter(name: String, version: String) = "org.junit.jupiter:junit-jupiter-$name:$version"
