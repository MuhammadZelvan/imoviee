plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.zelvan.imoviee"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zelvan.imoviee"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.firestore.ktx) // Ini harusnya juga di bawah BOM biar versinya ditarik BOM
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // Firebase BOM - Selalu letakkan di paling atas dependensi Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // <-- Versi BOM lo

    // Firebase Auth (uses BOM for version)
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google Sign-in - UPDATE VERSI INI
    implementation("com.google.android.gms:play-services-auth:21.0.0") // <--- UBAH VERSI INI!

    implementation ("com.google.android.flexbox:flexbox:3.0.0")


    // Firebase Firestore (setelah BOM agar versi ditarik oleh BOM)
    // Sebaiknya, pindahkan ini juga ke bawah BOM agar versinya disamakan oleh BOM.
    // implementation("com.google.firebase:firebase-firestore-ktx") // Jika mau versi ditarik BOM
}