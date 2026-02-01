plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "site.meowcat.loglet"
    compileSdk = 36

    defaultConfig {
        applicationId = "site.meowcat.loglet"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.ads)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // Play Services for Location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // OSMDroid for OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
