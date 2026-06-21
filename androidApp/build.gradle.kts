plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("com.google.devtools.ksp") }
android {
    namespace = "com.freelance.hores"
    compileSdk = 35
    defaultConfig { applicationId = "com.freelance.hores"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0.0" }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }
}
dependencies { implementation(project(":shared")); implementation("androidx.compose.ui:ui"); implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2"); implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("io.insert-koin:koin-android:3.5.3"); implementation("io.insert-koin:koin-compose:1.1.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
}
