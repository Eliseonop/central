/**
 * :composeApp — Kotlin Multiplatform shared library.
 *
 * Uses the standard kotlinMultiplatform + androidLibrary combination.
 *
 * Note: androidTarget() carries a deprecation *warning* in AGP 8.x, but it
 * continues to work correctly and JetBrains' own CMP 1.10.0 templates still
 * use this pattern. The alternative unified plugin
 * (com.android.kotlin.multiplatform.library) does NOT register the standard
 * kotlin {} KMP extension, making iosArm64() / sourceSets / androidMain etc.
 * all unresolvable when iOS targets are also declared — so we stay here.
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
plugins {
    // KMP + Android library
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    // Compose Multiplatform
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    // Kotlin Serialization
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // ── Android target ───────────────────────────────────────────────────────
    // androidTarget() deprecation warning is harmless until AGP 9.0 ships.
    @Suppress("DEPRECATION")
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // ── iOS targets ─────────────────────────────────────────────────────────
    val xcf = XCFramework("ComposeApp")
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            // Ktor Android engine
            implementation(libs.ktor.client.android)
            // Koin Android
            implementation(libs.koin.android)
            // Google Play Services – Fused Location Provider
            implementation(libs.gms.play.services.location)
            // OkHttp WebSocket (for SocketService)
            implementation(libs.okhttp)
            // ── CameraX + ML Kit — QR scanning ───────────────────────────────
            implementation("androidx.camera:camera-camera2:1.4.1")
            implementation("androidx.camera:camera-lifecycle:1.4.1")
            implementation("androidx.camera:camera-view:1.4.1")
            implementation("com.google.mlkit:barcode-scanning:17.3.0")
        }

        iosMain.dependencies {
            // Ktor Darwin (iOS/macOS) engine
            implementation(libs.ktor.client.darwin)
        }

        commonMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            // Material Icons (Icons.Default.*, Icons.AutoMirrored.*)
            implementation(compose.materialIconsExtended)
            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Coroutines
            implementation(libs.kotlinx.coroutinesCore)
            // Serialization
            implementation(libs.kotlinx.serializationJson)
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
            // Navigation
            implementation(libs.navigation.compose)
            // Multiplatform Settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noarg)
            // DateTime
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace  = "com.tcontur.central"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        // No applicationId / versionCode / versionName — those live in :androidApp
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(files("..\\shared\\libs\\protobin-0.1.1.jar"))
    debugImplementation(libs.compose.uiTooling)
}
