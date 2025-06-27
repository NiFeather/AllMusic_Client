pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0" apply true
}

rootProject.name = "AllMusic_Client"