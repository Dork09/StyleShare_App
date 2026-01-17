import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)

    // Nav Graph + SafeArgs
    alias(libs.plugins.navigation.safeargs)

    // Firebase Auth
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.styleshare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.styleshare"
        minSdk = 24
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}
        kotlin {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }


dependencies {
    // בסיס UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)

    // MVVM - ViewModel + LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation + SafeArgs
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // ROOM (SQLite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Firebase Auth בלבד (לא Storage/Firestore)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // אם אתן משתמשות מיקום/התראות לפי מיקום (אופציונלי)
    implementation(libs.play.services.location)
}
