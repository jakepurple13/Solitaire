group = "com.programmersbox"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version libs.versions.kotlin.version.get() apply false
    alias(libs.plugins.compose.compiler) apply false
    kotlin("multiplatform") version libs.versions.kotlin.version.get() apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("org.jetbrains.compose") version libs.versions.compose.version.get() apply false
}

buildscript {
    dependencies {
        classpath(libs.realm.plugin)
    }
}