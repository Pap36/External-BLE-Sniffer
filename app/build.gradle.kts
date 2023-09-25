plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidApplicationComposeConventionPlugin.kt
    alias(libs.plugins.nordic.application.compose)
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidHiltConventionPlugin.kt
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "com.externalblesniffer"

    defaultConfig {
        applicationId = "com.externalblesniffer"
        minSdk = 26
        compileSdk = 34
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)

    // Extended icons
    implementation(libs.androidx.compose.material.iconsExtended)
}