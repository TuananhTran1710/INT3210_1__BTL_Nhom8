import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.google.services)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

// Read the API key from local.properties using idiomatic Kotlin
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.reader().use { reader ->
        localProperties.load(reader)
    }
}

android {
    namespace = "com.example.wink"
    compileSdk = 36
    packaging {
        resources {
            // Khi gặp file trùng lặp, chọn file đầu tiên tìm thấy thay vì báo lỗi
            pickFirst("META-INF/LICENSE.md")
            pickFirst("META-INF/LICENSE-notice.md")
        }
    }
    defaultConfig {
        applicationId = "com.example.wink"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Correctly define the build config field. The value must be a String literal in Java/Kotlin.
        val apiKey = localProperties.getProperty("OPENAI_API_KEY", "")
        buildConfigField("String", "OPENAI_API_KEY", "\"$apiKey\"")

        val openRouterKey = localProperties.getProperty("OPENROUTER_API_KEY", "")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
    }
}

dependencies {

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.x") // Kiểm tra version mới nhất
    testImplementation("io.mockk:mockk:1.13.x")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")
    androidTestImplementation("io.mockk:mockk-agent:1.13.10")

    // 2. Compose Testing (để sửa lỗi createComposeRule, onNodeWithText...)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8") // Kiểm tra version compose của bạn
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.x.x")
    // Needed for createComposeRule, but not createAndroidComposeRule:
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.x.x")

    // Mockk for Android
    androidTestImplementation("io.mockk:mockk-android:1.13.x")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.x")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.animation)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(platform(libs.firebase.bom)) // Quan trọng: Dùng platform cho BOM
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    // Removed the hardcoded navigation-compose dependency: implementation("androidx.navigation:navigation-compose:2.7.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.github.jeziellago:compose-markdown:0.5.7")
    implementation("nl.dionsegijn:konfetti-compose:2.0.4")
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.1")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")
    androidTestImplementation("io.mockk:mockk-agent:1.13.10")

    // Các thư viện test khác cần thiết
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit & Kotlinx Serialization
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

}

kapt {
    correctErrorTypes = true
}