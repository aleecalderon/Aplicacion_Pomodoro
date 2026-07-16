plugins {
<<<<<<< HEAD
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.tasktimer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tasktimer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
=======
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.investigacion1"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.investigacion1"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Aquí activamos View Binding para que tus compañeros puedan conectar las vistas fácilmente
    buildFeatures {
        viewBinding = true
>>>>>>> 5215dd0fbe865b625229fdef8e4fc15d7be3b867
    }

    buildTypes {
        release {
<<<<<<< HEAD
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
=======
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
>>>>>>> 5215dd0fbe865b625229fdef8e4fc15d7be3b867
    }
}

dependencies {
<<<<<<< HEAD
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ViewModel + LiveData/StateFlow + SavedStateHandle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.4")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
=======
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
>>>>>>> 5215dd0fbe865b625229fdef8e4fc15d7be3b867
