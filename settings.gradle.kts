pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
        maven(url = "https://jitpack.io")
    }
    versionCatalogs {
        create("libs") {
            from("no.nordicsemi.android.gradle:version-catalog:1.9.10")
        }
    }
}

rootProject.name = "External-BLE-Sniffer"
include(":app")
include(":usb")
include(":blescanner")
include(":repo")
include(":ui:devices")
include(":ui:selected")
