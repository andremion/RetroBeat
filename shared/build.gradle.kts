plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)

            implementation(libs.kotlinx.coroutines)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel)
            implementation(libs.precompose.koin)
            implementation(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
            implementation(libs.androidx.exoplayer)
            implementation(libs.koin.android)
        }
    }
}

android {
    namespace = "io.github.andremion.lplayer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
