plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.fruit_camera"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fruit_camera"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
//    implementation("org.pytorch:pytorch_android_lite:1.12.2")
//    implementation("org.pytorch:pytorch_android_torchvision:1.12.2") {
//        exclude(group = "org.pytorch", module = "pytorch_android")
//    }

    implementation ("org.pytorch:pytorch_android:1.12.2")
    implementation ("org.pytorch:pytorch_android_torchvision:1.12.2")


    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}