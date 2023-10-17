plugins {
    alias(libs.plugins.nordic.library.compose)
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "com.externalblesniffer.usb"
    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":repo"))
    implementation("com.github.mik3y:usb-serial-for-android:3.7.0")
}