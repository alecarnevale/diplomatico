plugins {
    alias(libs.plugins.jetbrains.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinter)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
