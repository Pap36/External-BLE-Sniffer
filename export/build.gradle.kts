plugins {
    alias(libs.plugins.nordic.library.compose)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.externalblesniffer.export"
}

dependencies {
    implementation(project(":repo"))
    // JSON
    implementation(libs.kotlinx.serialization.json)
}