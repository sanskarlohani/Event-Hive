plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    //firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sanskar.eventhive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sanskar.eventhive"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("io.mockk:mockk-android:1.14.11")
    androidTestImplementation("io.mockk:mockk-agent:1.14.11")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("com.google.dagger:hilt-android:2.57")
    ksp("com.google.dagger:hilt-compiler:2.57")

    // Hilt Navigation for Compose
    implementation ("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Lifecycle
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")


    //Navigation
    implementation("androidx.navigation:navigation-compose:2.9.1")


    //material extended icon
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")

    //google play service
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("androidx.credentials:credentials:1.5.0")
    implementation ("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation(libs.firebase.database.ktx)
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    debugImplementation("com.google.firebase:firebase-appcheck-debug")

    //datastore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    //splashscreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    //coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    //notification
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")


    // For ImageBitmap
    implementation("com.google.zxing:core:3.5.3")
    implementation("androidx.compose.ui:ui-graphics:1.5.0") // For ImageBitmap

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.1.0")

    // Coroutine support (Kotlinx coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")



}
