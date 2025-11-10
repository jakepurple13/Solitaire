plugins {
    alias(libs.plugins.compose.compiler)
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    kotlin("native.cocoapods")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    androidTarget {
        /*compilations.all {
            kotlin {
                compilerOptions {
                    jvmToolchain(11)
                }
            }
        }*/
    }
    jvm("desktop") {
        compilations.all {
            kotlin {
                compilerOptions {
                    jvmToolchain(17)
                }
            }
        }
    }

    applyDefaultHierarchyTemplate()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.materialIconsExtended)
                implementation(compose.material3)
                api(libs.kotlinx.datetime)
                api(libs.datastore.core)
                api(libs.datastore.preferences)
                api(libs.androidx.room.runtime)
            }
        }
    }
}

dependencies {
    add("ksp", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    compileSdk = 34
    //sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "com.programmersbox.storage"
}
