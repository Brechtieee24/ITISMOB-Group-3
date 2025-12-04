plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.10" // Kotlin version
    id("com.google.gms.google-services") // Firebase plugin
}

android {
    namespace = "com.mobdeve.s16.group3.albrechtgabriel.lovelink"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mobdeve.s16.group3.albrechtgabriel.lovelink"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)

    // Java Time
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Geolocation
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("com.firebaseui:firebase-ui-database:8.0.2")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.1")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    // Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx:22.2.0")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Cloud Storage
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.firebaseui:firebase-ui-storage:8.0.2")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.6.0")

    // QR Generator
    implementation("com.google.zxing:core:3.5.1")

    // Coroutine support for Firebase tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
