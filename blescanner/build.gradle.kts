plugins {
    alias(libs.plugins.nordic.library.compose)
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "com.externalblesniffer.blescanner"
}

dependencies {
    implementation(project(":repo"))
}