plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("com.google.devtools.ksp")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }
    jvm("desktop")

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

                val roomVersion = "2.7.0-alpha07"
                implementation("androidx.room:room-runtime:$roomVersion")
                implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha07")
                implementation("androidx.sqlite:sqlite:2.5.0-alpha07")

                // Koin 3.5.6 LTS
                // NOTA: viewModel{} DSL no existeix a commonMain en Koin 3.5.x
                // Usem factory{} al commonModule() i koinViewModel() a les pantalles
                implementation("io.insert-koin:koin-core:3.5.6")
                implementation("io.insert-koin:koin-compose:1.1.2")

                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.insert-koin:koin-android:3.5.6")
                implementation("androidx.activity:activity-compose:1.9.2")

                // El codi generat per KSP (instantiateImpl) s'afegeix automàticament
                // al sourceSet androidMain via la tasca kspAndroid.
                // Forcem la dependència explícita per garantir l'ordre de compilació:
                kotlin.srcDir("build/generated/ksp/android/androidDebug/kotlin")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                kotlin.srcDir("build/generated/ksp/desktop/desktopMain/kotlin")
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

// Forçar que la compilació de Kotlin Android depengui del KSP Android
// per garantir que instantiateImpl estigui generat abans de compilar
afterEvaluate {
    tasks.matching { it.name == "compileDebugKotlinAndroid" }.configureEach {
        dependsOn("kspDebugKotlinAndroid")
    }
    tasks.matching { it.name == "compileReleaseKotlinAndroid" }.configureEach {
        dependsOn("kspReleaseKotlinAndroid")
    }
}

dependencies {
    val roomVersion = "2.7.0-alpha07"
    add("kspCommonMainMetadata", "androidx.room:room-compiler:$roomVersion")
    add("kspAndroid", "androidx.room:room-compiler:$roomVersion")
    add("kspDesktop", "androidx.room:room-compiler:$roomVersion")
    add("kspAndroidTest", "androidx.room:room-compiler:$roomVersion")
}
