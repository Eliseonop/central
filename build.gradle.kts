plugins {
    // loaded once to avoid multiple classloader issues in subprojects
    alias(libs.plugins.androidApplication)  apply false  // :androidApp
    alias(libs.plugins.androidLibrary)      apply false  // kept for potential future modules
    alias(libs.plugins.kmpAndroidLibrary)   apply false  // kept for reference; :composeApp uses kotlinMultiplatform + androidLibrary instead
    alias(libs.plugins.kotlinMultiplatform) apply false  // kept for potential future pure-KMP modules
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler)     apply false
    alias(libs.plugins.composeHotReload)    apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinAndroid)       apply false  // :androidApp
}
