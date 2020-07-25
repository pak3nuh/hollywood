import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import pt.pak3nuh.hollywood.gradle.Dependencies

plugins {
    kotlin("jvm") version pt.pak3nuh.hollywood.gradle.Versions.kotlin
    idea
    id("io.gitlab.arturbosch.detekt") version "1.10.0"
}

val kotlinFiles = "**/*.kt"
val buildFiles = "**/build/**"
val configFile = file("$rootDir/config/detekt/detekt.yml")
val formatConfigFile = file("$rootDir/config/detekt/format.yml")
val baselineFile = file("$rootDir/config/detekt/baseline.xml")

idea {
    project {
        jdkName = "1.8"
        languageLevel = IdeaLanguageLevel("1.8")
    }
}

allprojects {
    version = "0.1.3"
    group = "pt.pak3nuh.hollywood"

    repositories {
        jcenter()
    }
}

subprojects {

    apply(plugin = "kotlin")
    apply(plugin = "java-library")
    apply(plugin = "idea")
    apply(plugin = "maven")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation(Dependencies.kotlinStdLib)

        testImplementation(Dependencies.junitApi)
        testRuntimeOnly(Dependencies.junitEngine)
        testImplementation(Dependencies.assertK)
    }

    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "1.8"
    }

    detekt {
        buildUponDefaultConfig = true
        baseline = baselineFile

        reports {
            html.enabled = true
        }
    }
}
