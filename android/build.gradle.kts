plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

group = "com.programmersbox"
version = "2.0.0"

android {
    compileSdk = 35
    defaultConfig {
        applicationId = "com.programmersbox.solitaire"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "2.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        create("beta") {
            initWith(getByName("debug"))
            matchingFallbacks.addAll(listOf("debug", "release"))
            isDebuggable = false
        }
    }

    namespace = "com.programmersbox.solitaire"
}

dependencies {
    implementation(project(":common"))
    implementation(libs.androidx.activity.compose)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.ui:ui-test-junit4-android:1.7.0-beta05")
}

tasks.register("BuildAndRun") {
    doFirst {
        exec {
            workingDir(projectDir.parentFile)
            commandLine("./gradlew", "android:build")
            commandLine("./gradlew", "android:installDebug")
        }
    }
}