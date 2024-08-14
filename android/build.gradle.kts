plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.programmersbox.solitaire"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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