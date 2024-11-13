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

include(":annotations")
include(":processors")
include(":demo:app")
include(":demo:core-entities")
include(":demo:entities")
include(":demo:contributes")
