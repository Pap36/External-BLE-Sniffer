plugins {
    alias(libs.plugins.nordic.library.compose)
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "com.externalblesniffer.repo"
}

dependencies {

    implementation("com.github.mik3y:usb-serial-for-android:3.6.0")
}