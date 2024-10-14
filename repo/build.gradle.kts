plugins {
    alias(libs.plugins.nordic.library.compose)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.externalblesniffer.repo"
}

dependencies {

    implementation("com.github.mik3y:usb-serial-for-android:3.8.0")
    // JSON
    implementation(libs.kotlinx.serialization.json)
}