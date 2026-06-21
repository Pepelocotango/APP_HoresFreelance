plugins { kotlin("jvm"); id("org.jetbrains.compose") }
dependencies { implementation(project(":shared")); implementation(compose.desktop.currentOs); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1") }
compose.desktop { application { mainClass = "com.freelance.hores.MainKt"
    nativeDistributions { targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
        packageName = "HoresFreelance"; packageVersion = "1.0.0" } } }
