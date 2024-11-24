


plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.assetsync"
    compileSdk = 34


    buildFeatures {
        dataBinding = true  // For enabling data binding
    }

    defaultConfig {
        applicationId = "com.example.assetsync"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Enable minification for release builds
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
    // Microsoft SQL Server JDBC Driver
    implementation("com.microsoft.sqlserver:mssql-jdbc:9.2.1.jre11")


    // Image loading library (choose one)
    implementation("com.github.bumptech.glide:glide:4.15.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.0")
    implementation("com.squareup.picasso:picasso:2.71828") // Uncomment if using Picasso
    implementation ("androidx.databinding:databinding-runtime:7.0.0")


    // AndroidX Libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.1.0")) // Import the BoM
    implementation("com.google.firebase:firebase-auth") // Use BoM to manage versions
    implementation("com.google.firebase:firebase-database") // Use BoM to manage versions
    implementation("com.google.firebase:firebase-firestore") // Use BoM to manage versions
    implementation("com.google.firebase:firebase-storage-ktx") // Use BoM to manage versions
    implementation ("com.google.firebase:firebase-firestore-ktx:24.4.1") // Update to the latest version
    implementation ("com.google.firebase:firebase-storage-ktx:20.1.0") // Update to the latest version
    implementation ("androidx.appcompat:appcompat:1.5.1") // Ensure this is included

    // Testing Libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
