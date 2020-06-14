import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import pt.pak3nuh.hollywood.gradle.Dependencies

plugins {
    kotlin("jvm") version "1.3.72"
    idea
}

idea {
    project {
        jdkName = "1.8"
        languageLevel = IdeaLanguageLevel("1.8")
    }
}

allprojects {
    version = "0.1.2"
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
}
