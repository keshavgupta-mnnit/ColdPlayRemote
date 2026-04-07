plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.kglabs.wristdj"
    compileSdk = Versions.compileSdk

    defaultConfig {
        applicationId = "com.kglabs.wristdj"

        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = Versions.versionCode
        versionName = Versions.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    applicationVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

dependencies {
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.appcompat)

    testImplementation(Dependencies.junit)
    androidTestImplementation(Dependencies.junitExt)
    androidTestImplementation(Dependencies.espressoCore)

    implementation(Dependencies.composeUi)
    implementation(Dependencies.composeMaterial3)

    implementation(Dependencies.composePreview)
    implementation(Dependencies.lifecycleRuntime)
    implementation(Dependencies.activityCompose)
    androidTestImplementation(Dependencies.composeJunit)
    debugImplementation(Dependencies.composeUiTool)

    implementation(Dependencies.navigationCompose)

    implementation(Dependencies.destinationCompose)
    ksp(Dependencies.destinationComposeKsp)

    implementation(Dependencies.timber)

    implementation(platform(Dependencies.firebase))
    implementation(Dependencies.firebaseAnalytics)
    implementation(Dependencies.firebaseCrashlytics)

    implementation(Dependencies.hilt)
    kapt(Dependencies.hiltCompiler)
    implementation(Dependencies.hiltNavigationCompose)

    implementation(Dependencies.coroutines)
}