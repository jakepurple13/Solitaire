plugins {
    alias(libs.plugins.compose.compiler)
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.android.kotlin.multiplatform.library)
    kotlin("native.cocoapods")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    android {
        namespace = "com.programmersbox.storage"
        compileSdk = 37
        minSdk = 24
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
    add("kspAndroid", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

