plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.programmersbox.baselineprofile"
    compileSdk = 37

    defaultConfig {
        // Baseline profile generation requires API 28+.
        minSdk = 28
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":android"

    testOptions.managedDevices.localDevices {
        create("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            // AOSP images are recommended for profile generation (no Play Services noise).
            systemImageSource = "aosp"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

baselineProfile {
    // Generate on the Gradle Managed Device by default (used by CI).
    // Pass -Pbaselineprofile.useConnectedDevices=true to use a connected phone or emulator instead.
    val useConnected = providers.gradleProperty("baselineprofile.useConnectedDevices").orNull.toBoolean()
    useConnectedDevices = useConnected
    if (!useConnected) managedDevices += "pixel6Api34"
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    onVariants { variant ->
        val artifactsLoader = variant.artifacts.getBuiltArtifactsLoader()
        variant.instrumentationRunnerArguments.put(
            "targetAppId",
            variant.testedApks.map { artifactsLoader.load(it)?.applicationId!! }
        )
    }
}
