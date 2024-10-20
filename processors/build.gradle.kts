plugins {
    alias(libs.plugins.jetbrains.kotlin)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":annotations"))

    implementation(libs.ksp)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.ksp.testing)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
