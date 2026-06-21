plugins { kotlin("multiplatform"); id("com.android.library"); id("org.jetbrains.compose"); id("com.google.devtools.ksp") }
kotlin {
    androidTarget { compilations.all { kotlinOptions { jvmTarget = "17" } } }
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime); implementation(compose.foundation); implementation(compose.material3); implementation(compose.ui); implementation(compose.components.resources)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1"); implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.2"); implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
                val roomVersion = "2.7.0-alpha01"
                implementation("androidx.room:room-runtime:$roomVersion"); implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha01")
                implementation("io.insert-koin:koin-core:3.5.3"); implementation("io.insert-koin:koin-compose:1.1.2")
            }
        }
        val androidMain by getting { dependencies { implementation("androidx.room:room-runtime:2.7.0-alpha01") } }
        val desktopMain by getting { dependencies { implementation(compose.desktop.currentOs); implementation("androidx.room:room-runtime:2.7.0-alpha01") } }
    }
}
android { namespace = "com.freelance.hores.shared"; compileSdk = 35; defaultConfig { minSdk = 26 } }
dependencies {
    add("kspCommonMainMetadata", "androidx.room:room-compiler:2.7.0-alpha01")
    add("kspAndroid", "androidx.room:room-compiler:2.7.0-alpha01")
    add("kspDesktop", "androidx.room:room-compiler:2.7.0-alpha01")
}
