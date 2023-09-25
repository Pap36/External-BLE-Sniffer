// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false

    // Nordic plugins are defined in https://github.com/NordicSemiconductor/Android-Gradle-Plugins
    alias(libs.plugins.nordic.application) apply false
    alias(libs.plugins.nordic.application.compose) apply false
    alias(libs.plugins.nordic.library.compose) apply false
    alias(libs.plugins.nordic.library) apply false
    alias(libs.plugins.nordic.kotlin) apply false
    alias(libs.plugins.nordic.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}

extra {
    extra["kotlin_language_version"] = "1.9"
    extra["material3_version"] = "1.2.0-alpha07"
    extra["mesh_version"] = "1.0.0"
    extra["room_version"] = "2.6.0-beta01"
    extra["accompanist_version"] = "0.32.0"
    extra["paging_version"] = "3.2.1"
}