import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.compose.compiler)
}

group = "com.programmersbox"
version = "6.0.1"

kotlin {
    jvm {
        compilations.all {
            kotlin {
                compilerOptions {
                    jvmToolchain(17)
                }
            }
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Solitaire"
            packageVersion = libs.versions.appVersion.get()
        }
    }
}
