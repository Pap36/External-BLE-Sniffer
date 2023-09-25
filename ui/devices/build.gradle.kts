plugins {
    alias(libs.plugins.nordic.library.compose)
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "com.externalblesniffer.ui.devices"
}

dependencies {

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)

    // add nordic theme
    implementation(libs.nordic.theme)
    // add nordic navigation
    implementation(libs.nordic.navigation)

    // Extended icons
    implementation(libs.androidx.compose.material.iconsExtended)
}