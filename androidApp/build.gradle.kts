/**
 * :androidApp — thin Android launcher module.
 *
 * Responsibility: host the Application class, MainActivity, and the final
 * AndroidManifest.xml (with all permissions). All business logic lives in
 * the :composeApp KMP library.
 *
 * Per https://kotl.in/kmp-project-structure-migration this separation is
 * required for compatibility with Android Gradle Plugin 9.0+.
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)   // needed: setContent { App() } is @Composable
}

android {
    namespace  = "com.tcontur.central"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.tcontur.central"
        minSdk        = libs.versions.android.minSdk.get().toInt()
        targetSdk     = libs.versions.android.targetSdk.get().toInt()
        versionCode   = 1
        versionName   = "1.0"
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Use the compilerOptions DSL (replaces deprecated kotlinOptions)
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // The entire KMP library (shared + androidMain code)
    implementation(project(":composeApp"))

    // Activity Compose needed for setContent { }
    implementation(libs.androidx.activity.compose)

    // Koin Android needed in Application.onCreate()
    implementation(libs.koin.android)
}
