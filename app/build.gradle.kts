plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    // Google plugin to read google-services.json
    id("com.google.gms.google-services")
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_compiler_config.conf"))
}

android {
    namespace = "micheli.giorgio.pillsoclock"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "micheli.giorgio.pillsoclock"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE") as String)
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
        }
    }


    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx.v2100)
    // Icons
    implementation(libs.androidx.compose.material.icons.core)
    // Room database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    // opzionale ma quasi sempre utile, per le funzioni suspend/Flow nei DAO
    implementation(libs.androidx.room.ktx)
    // Navigation 3 libraries
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    // Kotlinx serialization
    implementation(libs.kotlinx.serialization.core)
    // Workmanager, componente di Jetpack
    implementation(libs.androidx.work.runtime.ktx)
    // Constraint layout utilizzato per lo stato vuoto della schermata "Medicinali"
    implementation(libs.androidx.constraintlayout.compose)
    // API Splashscreen di android
    implementation(libs.androidx.core.splashscreen)
    // DataStore (Preferences), per le impostazioni utente (es. dark mode)
    implementation(libs.androidx.datastore.preferences)

    // Firebase bom
    implementation(platform(libs.firebase.bom))
    // Firebase analytics
    implementation(libs.firebase.analytics)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}