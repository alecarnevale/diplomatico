pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "diplomatico"

includeBuild("gradle/plugins")

include(":api")
include(":processors")
include(":demo:app")
