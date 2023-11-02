plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
}

android {
    namespace = "com.example.web_server_poc"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.web_server_poc"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.jakewharton.timber:timber:5.0.1")
    // Rx
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    implementation("com.github.tbruyelle:rxpermissions:0.12")

    // WebRTC
//    implementation("io.getstream:stream-webrtc-android:1.0.7")
//    implementation("io.github.webrtc-sdk:android:114.5735.05")
    implementation("org.webrtc:google-webrtc:1.0.32006")
//    implementation("com.dafruits:webrtc:117.0.0")

    implementation("androidmads.library.qrgenearator:QRGenearator:1.0.4")
    implementation("com.google.zxing:core:3.3.3")
    implementation("com.journeyapps:zxing-android-embedded:3.6.0@aar")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}