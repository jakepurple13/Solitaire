package com.programmersbox.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the Baseline Profile and Startup Profile for the app.
 *
 * Generate on the Gradle Managed Device (same as CI):
 *   ./gradlew :android:generateReleaseBaselineProfile \
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.programmersbox.baselineprofile.BaselineProfileGenerator
 *
 * Add `-Pbaselineprofile.useConnectedDevices=true` to use a connected rooted or API 33+ device instead.
 * The class filter keeps [StartupBenchmarks] out of the run; otherwise the plugin can fail to
 * find the generated profile. Output is written to `android/src/release/generated/baselineProfiles/`.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),
            // Also emit a startup profile so AGP can optimize dex layout for startup.
            includeInStartupProfile = true,
            // A few iterations are enough for a stable profile and keep generation fast on CI.
            maxIterations = 3,
            stableIterations = 2,
        ) {
            pressHome()
            startActivityAndWait()
            solitaireJourney()
        }
    }
}
