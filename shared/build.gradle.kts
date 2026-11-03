import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * The shared part of AsmanPay: business logic, screens, theme and translations.
 * It's built once for Android (used by :app) and once for iOS (a framework
 * called "Shared" that the Xcode project in iosApp/ links).
 */
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidLibrary {
        namespace = "com.eriktemiraliev.asmanpay.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        // Compose Multiplatform resources (strings.xml) need Android resources switched on.
        androidResources {
            enable = true
        }
        // Runs the tests in src/commonTest on the computer's JVM (no phone needed).
        withHostTest {}
    }

    listOf(
        iosArm64(),           // real iPhones
        iosSimulatorArm64()   // the iOS simulator on Apple-silicon Macs (M1/M2/...)
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

compose.resources {
    // Generated as com.eriktemiraliev.asmanpay.resources.Res, e.g. Res.string.app_name
    packageOfResClass = "com.eriktemiraliev.asmanpay.resources"
    publicResClass = false
}
