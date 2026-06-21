Aquí el tens, llest per copiar i enganxar:

---

# Prompt per a l'agent de codi: Correcció completa del monorepo KMP HoresFreelance

## Context del projecte

Estem treballant en un monorepo Kotlin Multiplatform (KMP) anomenat **HoresFreelance** amb l'estructura:
- `androidApp/` — app Android (captura d'hores, fitxatge mòbil)
- `desktopApp/` — app Desktop JVM per a Linux/Windows/macOS (gestió i control ofimàtic)
- `shared/` — codi compartit amb sourceSets `commonMain`, `androidMain`, `desktopMain`

La BD és **Room KMP** (`androidx.room:room-runtime:2.7.0-alpha01`) amb `BundledSQLiteDriver`. La DI és **Koin**. La UI és **Compose Multiplatform**.

---

## Llista de correccions a fer

### CORRECCIÓ 1 — Eliminar Hilt completament de `androidApp`

**Fitxer: `androidApp/src/main/java/com/freelance/hores/HoresApp.kt`**

Substituir per:
```kotlin
package com.freelance.hores

import android.app.Application
import com.freelance.hores.di.initKoin
import org.koin.android.ext.koin.androidContext

class HoresApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@HoresApp)
        }
    }
}
```

**Fitxer: `androidApp/src/main/java/com/freelance/hores/MainActivity.kt`**

Eliminar `import dagger.hilt.android.AndroidEntryPoint` i l'anotació `@AndroidEntryPoint`. La classe ha de quedar com a `class MainActivity : ComponentActivity()` sense cap anotació Hilt.

**Fitxer: `androidApp/build.gradle.kts`**

Eliminar totes les dependències i plugins de Hilt/Dagger:
- Eliminar `id("com.google.devtools.ksp")` dels plugins (si només s'usava per Hilt; Room KMP usa el KSP del `shared`)
- Eliminar `implementation("com.google.dagger:hilt-android:...")` si existeix
- Eliminar `kapt("com.google.dagger:hilt-compiler:...")` o `ksp("...")` de Hilt si existeix

**Fitxer: `build.gradle.kts` (arrel)**

Eliminar el plugin `id("com.google.dagger.hilt.android") version "..." apply false"` si existeix.

---

### CORRECCIÓ 2 — Crear el sistema de DI Koin complet

**Fitxer NOU: `shared/src/commonMain/kotlin/com/freelance/hores/di/Koin.kt`**

```kotlin
package com.freelance.hores.di

import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.ui.screen.calendari.CalendariViewModel
import com.freelance.hores.ui.screen.clients.ClientsViewModel
import com.freelance.hores.ui.screen.dia.DiaDetallViewModel
import com.freelance.hores.ui.screen.fitxar.FitxarViewModel
import com.freelance.hores.ui.screen.registre.RegistreViewModel
import com.freelance.hores.ui.screen.resum.ResumViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun commonModule() = module {
    single { RegistreRepository(get(), get(), get(), get(), get()) }

    viewModel { CalendariViewModel(get()) }
    viewModel { ClientsViewModel(get()) }
    viewModel { DiaDetallViewModel(get()) }
    viewModel { FitxarViewModel(get(), get()) }
    viewModel { RegistreViewModel(get()) }
    viewModel { ResumViewModel(get(), get()) }
}

expect fun platformModule(): org.koin.core.module.Module

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(commonModule(), platformModule())
    }
}
```

**Fitxer NOU: `shared/src/commonMain/kotlin/com/freelance/hores/di/AppPrefs.kt`**

```kotlin
package com.freelance.hores.di

interface AppPrefs {
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getString(key: String, defaultValue: String): String
    fun putBoolean(key: String, value: Boolean)
    fun putString(key: String, value: String)
}
```

---

### CORRECCIÓ 3 — Implementar `AppPrefs` per plataforma

**Fitxer: `shared/src/androidMain/kotlin/com/freelance/hores/di/AndroidPrefs.kt`** (NOU)

```kotlin
package com.freelance.hores.di

import android.content.SharedPreferences

class AndroidPrefs(private val prefs: SharedPreferences) : AppPrefs {
    override fun getBoolean(key: String, defaultValue: Boolean) =
        prefs.getBoolean(key, defaultValue)
    override fun getString(key: String, defaultValue: String) =
        prefs.getString(key, defaultValue) ?: defaultValue
    override fun putBoolean(key: String, value: Boolean) =
        prefs.edit().putBoolean(key, value).apply()
    override fun putString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()
}
```

**Fitxer: `shared/src/androidMain/kotlin/com/freelance/hores/di/Koin.kt`** (substituir actual)

```kotlin
package com.freelance.hores.di

import androidx.room.Room
import com.freelance.hores.data.db.AppDatabase
import com.freelance.hores.data.db.getRoomDatabase
import com.freelance.hores.data.export.ExportService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single<AppPrefs> {
        AndroidPrefs(
            androidContext().getSharedPreferences("hores_prefs", android.content.Context.MODE_PRIVATE)
        )
    }
    single {
        val dbFile = androidContext().getDatabasePath("hores_database.db").absolutePath
        getRoomDatabase(
            Room.databaseBuilder<AppDatabase>(
                context = androidContext(),
                name = dbFile,
                factory = { AppDatabase::class.instantiateImpl() }
            )
        )
    }
    single { get<AppDatabase>().diaDao() }
    single { get<AppDatabase>().concepteDao() }
    single { get<AppDatabase>().rangHorariDao() }
    single { get<AppDatabase>().clientDao() }
    single { ExportService(androidContext()) }
}
```

**Fitxer NOU: `shared/src/desktopMain/kotlin/com/freelance/hores/di/DesktopPrefs.kt`**

```kotlin
package com.freelance.hores.di

import java.io.File
import java.util.Properties

class DesktopPrefs : AppPrefs {
    private val props = Properties()
    private val prefFile = File(System.getProperty("user.home"), ".horesfreelance/settings.properties")

    init {
        if (prefFile.exists()) prefFile.inputStream().use { props.load(it) }
    }

    override fun getBoolean(key: String, defaultValue: Boolean) =
        props.getProperty(key)?.toBoolean() ?: defaultValue

    override fun getString(key: String, defaultValue: String) =
        props.getProperty(key) ?: defaultValue

    override fun putBoolean(key: String, value: Boolean) {
        props.setProperty(key, value.toString())
        save()
    }

    override fun putString(key: String, value: String) {
        props.setProperty(key, value)
        save()
    }

    private fun save() {
        prefFile.parentFile?.mkdirs()
        prefFile.outputStream().use { props.store(it, null) }
    }
}
```

**Fitxer: `shared/src/desktopMain/kotlin/com/freelance/hores/di/Koin.kt`** (substituir actual)

```kotlin
package com.freelance.hores.di

import androidx.room.Room
import com.freelance.hores.data.db.AppDatabase
import com.freelance.hores.data.db.getRoomDatabase
import com.freelance.hores.data.export.DesktopExportService
import org.koin.dsl.module
import java.io.File

actual fun platformModule() = module {
    single<AppPrefs> { DesktopPrefs() }
    single {
        val dbFile = File(System.getProperty("user.home"), ".horesfreelance/hores_database.db")
        dbFile.parentFile?.mkdirs()
        getRoomDatabase(
            Room.databaseBuilder<AppDatabase>(
                name = dbFile.absolutePath,
                factory = { AppDatabase::class.instantiateImpl() }
            )
        )
    }
    single { get<AppDatabase>().diaDao() }
    single { get<AppDatabase>().concepteDao() }
    single { get<AppDatabase>().rangHorariDao() }
    single { get<AppDatabase>().clientDao() }
    single { DesktopExportService() }
}
```

---

### CORRECCIÓ 4 — Eliminar la classe `DesktopPrefs` antiga

Eliminar el fitxer `shared/src/commonMain/kotlin/com/freelance/hores/util/DesktopPrefs.kt` sencer (queda substituït per les implementacions de plataforma de `AppPrefs`).

---

### CORRECCIÓ 5 — Corregir `FitxarViewModel` per usar `AppPrefs`

**Fitxer: `shared/src/commonMain/kotlin/com/freelance/hores/ui/screen/fitxar/FitxarViewModel.kt`**

Substituir el tipus `SharedPreferences` per `AppPrefs` i actualitzar tots els usos:

- Canviar `private val prefs: SharedPreferences` → `private val prefs: AppPrefs`
- Eliminar `import android.content.SharedPreferences`
- Afegir `import com.freelance.hores.di.AppPrefs`
- Canviar `prefs.getBoolean("is_fitxant", false)` → `prefs.getBoolean("is_fitxant", false)` *(igual, ja és compatible)*
- Canviar `prefs.getString("hora_inici_arrodonida", "") ?: ""` → `prefs.getString("hora_inici_arrodonida", "")`
- Canviar `prefs.getString("data_fitxatge", "") ?: ""` → `prefs.getString("data_fitxatge", "")`
- Canviar tots els blocs `prefs.edit().apply { putBoolean(...); putString(...); apply() }` per crides directes:
```kotlin
prefs.putBoolean("is_fitxant", true)
prefs.putString("hora_inici_arrodonida", roundedStr)
prefs.putString("data_fitxatge", todayStr)
```

---

### CORRECCIÓ 6 — Crear `ExportService` multiplataforma

**Fitxer NOU: `shared/src/commonMain/kotlin/com/freelance/hores/data/export/ExportService.kt`**

```kotlin
package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate

interface ExportService {
    fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate)
    fun exportPdf(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate)
}
```

> **Nota:** Aquí canviem a `kotlinx.datetime.LocalDate` (ja inclòs al `build.gradle.kts`) en lloc de `java.time.LocalDate`. Vegeu Correcció 9 per la migració completa de dates.

**Fitxer: `shared/src/androidMain/kotlin/com/freelance/hores/data/export/ExportService.kt`** (renombrar/adaptar l'actual)

L'actual `ExportService` d'Android ha de passar a ser la implementació Android de la interfície. Afegir `class ExportService(...) : com.freelance.hores.data.export.ExportService` i implementar els mètodes sobreescrivint-los. El cos intern (CSV, PDF, Intent, FileProvider) queda igual.

**Fitxer NOU: `shared/src/desktopMain/kotlin/com/freelance/hores/data/export/DesktopExportService.kt`**

```kotlin
package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate
import java.awt.Desktop
import java.io.File
import java.time.format.DateTimeFormatter

class DesktopExportService : ExportService {

    override fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val filename = "hores_freelance_${startDate}_${endDate}.csv"
        val file = File(System.getProperty("user.home"), ".horesfreelance/exports/$filename")
        file.parentFile?.mkdirs()
        file.writeText(buildCsvContent(dias))
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file)
    }

    override fun exportPdf(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val filename = "hores_freelance_${startDate}_${endDate}.pdf"
        val file = File(System.getProperty("user.home"), ".horesfreelance/exports/$filename")
        file.parentFile?.mkdirs()
        DesktopPdfExporter().exportToPdf(dias, file, startDate, endDate)
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file)
    }

    private fun buildCsvContent(dias: List<Dia>): String {
        val sb = StringBuilder()
        sb.appendLine("Data,Concepte,Preu/h,Inici,Fi,Durada (h),Total €,Despeses,Estat")
        for (dia in dias) {
            for (concepte in dia.conceptes) {
                for (rang in concepte.rangsHoraris) {
                    val duracio = rang.getDuracionaEnHoras()
                    val total = duracio * concepte.preuHora + concepte.despeses
                    sb.appendLine("${dia.data},${concepte.nom},${concepte.preuHora},${rang.horaInici},${rang.horaFi},%.2f,%.2f,${concepte.despeses},${concepte.estat}".format(duracio, total))
                }
            }
        }
        return sb.toString()
    }
}
```

**Fitxer NOU: `shared/src/desktopMain/kotlin/com/freelance/hores/data/export/DesktopPdfExporter.kt`**

```kotlin
package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate
import java.io.File

class DesktopPdfExporter {
    fun exportToPdf(dias: List<Dia>, file: File, startDate: LocalDate, endDate: LocalDate) {
        // Implementació bàsica amb text pur en un .pdf fals fins que s'integri una llibreria PDF JVM
        // TODO: substituir per iText7 o Apache PDFBox quan s'afegeixi la dependència
        val sb = StringBuilder()
        sb.appendLine("HoresFreelance — Informe $startDate / $endDate")
        sb.appendLine("=".repeat(60))
        for (dia in dias) {
            sb.appendLine("\n${dia.data}")
            for (concepte in dia.conceptes) {
                for (rang in concepte.rangsHoraris) {
                    val duracio = rang.getDuracionaEnHoras()
                    sb.appendLine("  ${concepte.nom} | ${rang.horaInici}-${rang.horaFi} | %.2fh | %.2f€".format(duracio, duracio * concepte.preuHora))
                }
            }
        }
        file.writeText(sb.toString())
    }
}
```

> **Nota per al futur:** Per generar PDF real a Desktop, afegir `implementation("com.itextpdf:itext7-core:7.2.5")` al `desktopMain` i substituir el cos de `DesktopPdfExporter`.

---

### CORRECCIÓ 7 — Corregir `ResumViewModel`: eliminar `android.content.Intent`

**Fitxer: `shared/src/commonMain/kotlin/com/freelance/hores/ui/screen/resum/ResumViewModel.kt`**

Substituir les funcions d'exportació:

```kotlin
// ELIMINAR aquestes funcions:
fun exportCsv(filteredDias: List<Dia>): android.content.Intent { ... }
fun exportPdf(filteredDias: List<Dia>): android.content.Intent { ... }

// SUBSTITUIR per:
fun exportCsv(filteredDias: List<Dia>) {
    val state = _resumState.value
    exportService.exportCsv(filteredDias, state.startDate, state.endDate)
}

fun exportPdf(filteredDias: List<Dia>) {
    val state = _resumState.value
    exportService.exportPdf(filteredDias, state.startDate, state.endDate)
}
```

Canviar el tipus de `exportService` a la interfície comuna:
```kotlin
private val exportService: com.freelance.hores.data.export.ExportService
```

---

### CORRECCIÓ 8 — Corregir `ResumScreen`: eliminar `LocalContext` i `startActivity`

**Fitxer: `shared/src/commonMain/kotlin/com/freelance/hores/ui/screen/resum/ResumScreen.kt`**

- Eliminar `val context = LocalContext.current`
- Eliminar `import androidx.compose.ui.platform.LocalContext`
- Substituir els `onClick` dels botons d'exportació:

```kotlin
// ELIMINAR:
onClick = {
    val intent = viewModel.exportCsv(filteredDias)
    context.startActivity(intent)
}

// SUBSTITUIR per:
onClick = { viewModel.exportCsv(filteredDias) }
```

Aplicar el mateix canvi per al botó d'exportació PDF.

Fer el mateix per a **tots els fitxers de `commonMain`** que continguin `LocalContext.current` i `context.startActivity(...)`: `RegistreScreen.kt`, `CalendariScreen.kt`.

---

### CORRECCIÓ 9 — Corregir `import` fora de `package` a `RegistreRepository`

**Fitxer: `shared/src/commonMain/kotlin/com/freelance/hores/data/repository/RegistreRepository.kt`**

La primera línia és `import androidx.room.withTransaction` i la segona és `package ...`. Posar la declaració `package` primera i els imports a continuació:

```kotlin
package com.freelance.hores.data.repository

import androidx.room.withTransaction
import com.freelance.hores.data.db.AppDatabase
// ... resta d'imports
```

---

### CORRECCIÓ 10 — Migrar `java.time.*` a `kotlinx-datetime` al `commonMain`

En tots els fitxers de `shared/src/commonMain/` fer les substitucions següents:

| `java.time` (eliminar) | `kotlinx.datetime` (usar) |
|---|---|
| `import java.time.LocalDate` | `import kotlinx.datetime.LocalDate` |
| `import java.time.LocalTime` | `import kotlinx.datetime.LocalTime` *(si disponible, altrament mantenir `java.time.LocalTime` per ara)* |
| `import java.time.YearMonth` | Substituir per càlcul manual amb `kotlinx.datetime.LocalDate` |
| `import java.time.format.DateTimeFormatter` | Usar `.toString()` i parsers de `kotlinx-datetime` o helpers manuals |
| `import java.util.Locale` | Eliminar; els formats de `kotlinx-datetime` no depenen de `Locale` |
| `import java.time.Instant` | `import kotlinx.datetime.Instant` |
| `import java.time.ZoneOffset` | `import kotlinx.datetime.TimeZone` i `kotlinx.datetime.toLocalDateTime` |

> **Prioritat:** Fer primer `LocalDate` (el més utilitzat) als models de domini i al repositori. Els `DateTimeFormatter` de la UI es poden resoldre amb funcions d'extensió `expect/actual` si el format és complex.

Fitxers a modificar (per ordre de prioritat):
1. `domain/model/Dia.kt` — `LocalDate`
2. `domain/model/RangHorari.kt` — `LocalTime`
3. `data/repository/RegistreRepository.kt` — `LocalDate`, `LocalTime`
4. `data/db/entity/DiaEntity.kt` — eliminar import `LocalDate` (no s'usa)
5. `ui/screen/*/` — tots els ViewModels i Screens

---

### CORRECCIÓ 11 — Actualitzar dependències Koin al `build.gradle.kts`

**Fitxer: `shared/build.gradle.kts`**

Al bloc `commonMain.dependencies`, substituir les línies de Koin per:

```kotlin
implementation("io.insert-koin:koin-core:3.5.3")
implementation("io.insert-koin:koin-compose:1.1.2")
implementation("io.insert-koin:koin-compose-viewmodel:1.1.2")
```

Al bloc `androidMain.dependencies`, afegir:
```kotlin
implementation("io.insert-koin:koin-android:3.5.3")
```

---

### CORRECCIÓ 12 — Actualitzar `desktopApp/Main.kt` per usar `initKoin`

**Fitxer: `desktopApp/src/jvmMain/kotlin/com/freelance/hores/Main.kt`**

Substituir la inicialització de Koin:

```kotlin
// ELIMINAR:
remember { startKoin { modules(commonModule(), platformModule()) } }

// SUBSTITUIR per:
remember { initKoin() }
```

Afegir l'import:
```kotlin
import com.freelance.hores.di.initKoin
```

---

### CORRECCIÓ 13 — Netejar imports residuals

En tots els fitxers de `commonMain` i `desktopMain`:
- Eliminar `import dagger.hilt.android.lifecycle.HiltViewModel`
- Eliminar `import dagger.hilt.android.AndroidEntryPoint`
- Eliminar `@HiltViewModel` i `@HiltAndroidApp` que quedin
- Eliminar `import javax.inject.Inject` i `@Inject` als ViewModels (Koin no en necessita)
- Eliminar `import org.koin.core.annotation.KoinExperimentalAPI` si no s'usa l'API experimental

---

## Ordre d'execució recomanat

1. Correccions 1 i 2 (eliminar Hilt, crear `commonModule`)
2. Correcció 3 (implementacions `AppPrefs` per plataforma)
3. Correcció 4 (eliminar `DesktopPrefs` antiga)
4. Correcció 5 (`FitxarViewModel` amb `AppPrefs`)
5. Correcció 6 (crear `ExportService` interfície + implementacions)
6. Correccions 7 i 8 (`ResumViewModel` + `ResumScreen` sense `Intent`)
7. Correcció 9 (arreglar l'`import` fora de `package`)
8. Correcció 11 (actualitzar `build.gradle.kts`)
9. Correcció 12 (`Main.kt` desktop)
10. Correccions 10 i 13 (migrar dates + netejar imports) — fer al final per no bloquejar els passos anteriors

---

## Verificació final

Un cop aplicats tots els canvis, confirmar que:
- [ ] `./gradlew :shared:compileCommonMainKotlinMetadata` passa sense errors
- [ ] `./gradlew :androidApp:assembleDebug` passa sense errors
- [ ] `./gradlew :desktopApp:run` arranca l'aplicació desktop sense crash
- [ ] Cap fitxer de `commonMain` conté `import android.*`
- [ ] Cap fitxer de `commonMain` conté `android.content.Intent` ni `LocalContext`
- [ ] Cap fitxer de `commonMain` o `desktopMain` conté `SharedPreferences`
- [ ] `commonModule()` registra tots els ViewModels i el repositori
