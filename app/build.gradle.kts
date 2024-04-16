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
    implementation(project(":ui:devices"))
    implementation(project(":ui:selected"))
    implementation(project(":usb"))
    implementation(project(":blescanner"))
    implementation(project(":export"))

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)

    // add nordic theme
    implementation(libs.nordic.theme)
    // add nordic navigation
    implementation(libs.nordic.navigation)
    // add nordic ble permission
    implementation(libs.nordic.permissions.ble)

    // Extended icons
    implementation(libs.androidx.compose.material.iconsExtended)
}