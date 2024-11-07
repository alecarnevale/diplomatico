plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlinter)
}

android {
    namespace = "com.alecarnevale.diplomatico.demo.entities"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":demo:core-entities"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.serialization.json)
}
