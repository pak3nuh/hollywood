import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import pt.pak3nuh.hollywood.gradle.Versions

plugins {
    kotlin("jvm") version "1.3.61"
    idea
}

idea {
    project {
        jdkName = "1.8"
        languageLevel = IdeaLanguageLevel("1.8")
    }
}

allprojects {
    version = "0.0.1"
    group = "pt.pak3nuh.hollywood"

    repositories {
        jcenter()
    }
}

subprojects {

    apply(plugin = "kotlin")
    apply(plugin = "java-library")
    apply(plugin = "idea")

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation(kotlin("stdlib-jdk8", Versions.kotlin))

        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
        testImplementation("com.willowtreeapps.assertk:assertk-jvm:${Versions.assertK}")
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
