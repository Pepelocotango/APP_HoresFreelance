plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
    // ELIMINAT: id("androidx.room")
    // El Room Gradle Plugin (RGP) és incompatible amb la configuració
    // manual de KSP multiplatform. Causa exactament l'error MissingType
    // perquè intenta configurar el KSP de forma diferent a kspCommonMainMetadata.
    // El bloc room { schemaDirectory(...) } també s'elimina per aquest motiu.
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }
    jvm("desktop")

    // AFEGIT: necessari per a expect object AppDatabaseConstructor sense actual explícit
    // Sense això, Kotlin 1.9.x genera un error de compilació per l'expect object del Room
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

                implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.2")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

                // CANVIAT: alpha01 → alpha07 (corregeix el bug MissingType de KSP)
                val roomVersion = "2.7.0-alpha07"
                implementation("androidx.room:room-runtime:$roomVersion")

                // CANVIAT: alpha01 → alpha07 (ha de coincidir amb room)
                implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha07")
                implementation("androidx.sqlite:sqlite:2.5.0-alpha07")

                // CANVIAT: Beta4 → estable (Beta4 té incompatibilitats amb compose estable)
                implementation("io.insert-koin:koin-core:3.5.6")
                implementation("io.insert-koin:koin-compose:1.1.5")
                implementation("io.insert-koin:koin-compose-viewmodel:1.1.5")

                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.insert-koin:koin-android:3.5.6")
                implementation("androidx.activity:activity-compose:1.9.2")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    namespace = "com.freelance.hores.shared"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// ELIMINAT: bloc room { schemaDirectory(...) }
// Aquest bloc pertany al Room Gradle Plugin que hem eliminat.
// Si vols exportar l'schema per a proves, pots afegir-ho via
// ksp arguments al build.gradle.kts de l'androidApp:
//   ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    // CANVIAT: room-compiler alpha01 → alpha07 (ha de coincidir amb room-runtime)
    val roomVersion = "2.7.0-alpha07"
    add("kspCommonMainMetadata", "androidx.room:room-compiler:$roomVersion")
    add("kspAndroid", "androidx.room:room-compiler:$roomVersion")
    add("kspDesktop", "androidx.room:room-compiler:$roomVersion")
    add("kspAndroidTest", "androidx.room:room-compiler:$roomVersion")
}
