import java.util.Properties

// ACRA - Load credentials from secret.properties
val secretProperties = Properties().apply {
    val file = rootProject.file("secret.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.cssnr.todolist"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.cssnr.todolist"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ACRA - Acrarium backend setup: https://www.acra.ch/docs/Setup
        buildConfigField("String", "ACRA_URI", "\"${secretProperties.getProperty("acra.uri") ?: ""}\"")
        buildConfigField("String", "ACRA_USER", "\"${secretProperties.getProperty("acra.user") ?: ""}\"")
        buildConfigField("String", "ACRA_PASS", "\"${secretProperties.getProperty("acra.pass") ?: ""}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.reveal.swipe)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.room3.runtime)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.androidx.room3.compiler)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.acra.http)
    implementation(libs.acra.toast)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
