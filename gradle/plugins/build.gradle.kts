repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.jetbrains.kotlin)
    alias(libs.plugins.kotlinter)
    id("java-gradle-plugin")
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("diplomaticoPlugin") {
            id = "io.github.alecarnevale.diplomatico"
            implementationClass = "com.alecarnevale.diplomatico.gradle.DiplomaticoPlugin"
            version = "1.0"
            displayName = "Diplomatico Plugin"
            description = ""
        }
    }
}
