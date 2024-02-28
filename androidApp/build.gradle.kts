plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "io.github.andremion.musicplayer.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.andremion.musicplayer.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.0.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val releaseKeyStoreFile = properties["releaseKeyStoreFile"]?.toString()
        ?: System.getenv("releaseKeyStoreFile")
    val releaseKeyStoreAlias = properties["releaseKeyStoreAlias"]?.toString()
        ?: System.getenv("releaseKeyStoreAlias")
    val releaseKeyStorePassword = properties["releaseKeyStorePassword"]?.toString()
        ?: System.getenv("releaseKeyStorePassword")
    val releaseKeysProvided =
        releaseKeyStoreFile != null && releaseKeyStoreAlias != null && releaseKeyStorePassword != null

    signingConfigs {
        if (releaseKeysProvided) {
            create("release") {
                storeFile = file(releaseKeyStoreFile)
                storePassword = releaseKeyStorePassword
                keyAlias = releaseKeyStoreAlias
                keyPassword = releaseKeyStorePassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            if (releaseKeysProvided) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(projects.shared)

    debugImplementation(compose.uiTooling)
    debugImplementation(compose.preview)
}
