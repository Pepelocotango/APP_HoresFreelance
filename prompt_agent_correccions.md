# Prompt per a l'agent de codi: Correccions pendents del monorepo KMP HoresFreelance

## Context del projecte

Monorepo Kotlin Multiplatform (KMP) **HoresFreelance** amb:
- `androidApp/` — app Android (captura d'hores, fitxatge mòbil)
- `desktopApp/` — app Desktop JVM per a Linux/Windows/macOS
- `shared/` — codi compartit amb sourceSets `commonMain`, `androidMain`, `desktopMain`

**Tecnologies:** Room KMP 2.7.0-alpha01, Koin 3.5.3, Compose Multiplatform, kotlinx-datetime 0.6.0

---

## Estat de les correccions prèvies

Les correccions v1 i v2 **NO estan aplicades al codebase actual**. Només 4 de 15 correccions estan fetes:
- ✅ v1#1: Hilt eliminat
- ✅ v1#2: Sistema Koin creat (però amb `factory` en lloc de `viewModel`)
- ✅ v1#3: AppPrefs per plataforma
- ✅ v1#8: ResumScreen sense LocalContext

**11 correccions PENDENTS** que cal aplicar + problemes addicionals descoberts.

---

## LLISTA COMPLETA DE CANVIS A FER

### BLOQUEJANT 1 — Room KMP: @ConstructedBy i AppDatabaseConstructor

**Fitxer:** `shared/src/commonMain/kotlin/com/freelance/hores/data/db/AppDatabase.kt`

**Problema:** Sense `@ConstructedBy`, el KSP de Room NO genera `instantiateImpl()` i la compilació falla.

**Substituir el fitxer sencer per:**

```kotlin
package com.freelance.hores.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.freelance.hores.data.db.dao.ClientDao
import com.freelance.hores.data.db.dao.ConcepteDao
import com.freelance.hores.data.db.dao.DiaDao
import com.freelance.hores.data.db.dao.RangHorariDao
import com.freelance.hores.data.db.entity.ClientEntity
import com.freelance.hores.data.db.entity.ConcepteEntity
import com.freelance.hores.data.db.entity.DiaEntity
import com.freelance.hores.data.db.entity.RangHorariEntity

@Database(
    entities = [DiaEntity::class, ConcepteEntity::class, RangHorariEntity::class, ClientEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaDao(): DiaDao
    abstract fun concepteDao(): ConcepteDao
    abstract fun rangHorariDao(): RangHorariDao
    abstract fun clientDao(): ClientDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    migrations: Array<Migration> = emptyArray()
): AppDatabase {
    return builder
        .addMigrations(*migrations)
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .setQueryCoroutineContext(kotlinx.coroutines.Dispatchers.IO)
        .build()
}
```

---

### BLOQUEJANT 2 — Dependències Koin al shared/build.gradle.kts

**Fitxer:** `shared/build.gradle.kts`

**Problema:** Falta `koin-compose-viewmodel` per poder usar `koinViewModel()` i el DSL `viewModel { }`.

**Al bloc `commonMain.dependencies`, afegir:**
```kotlin
implementation("io.insert-koin:koin-compose-viewmodel:1.1.2")
```

**Al bloc `androidMain.dependencies`, afegir:**
```kotlin
implementation("io.insert-koin:koin-android:3.5.3")
```

**Eliminar duplicats:** `room-runtime` apareix a `commonMain` i `androidMain`. Deixar-lo només a `commonMain`.

---

### BLOQUEJANT 3 — Corregir Koin.kt per usar viewModel DSL

**Fitxer:** `shared/src/commonMain/kotlin/com/freelance/hores/di/Koin.kt`

**Problema:** Actualment usa `factory` però ha de ser `viewModel` perquè Koin gestioni el cicle de vida correctament.

**Substituir:**
```kotlin
// CANVIAR factory per viewModel:
viewModel { CalendariViewModel(get()) }
viewModel { ClientsViewModel(get()) }
viewModel { DiaDetallViewModel(get()) }
viewModel { FitxarViewModel(get(), get()) }
viewModel { RegistreViewModel(get()) }
viewModel { ResumViewModel(get(), get()) }
```

---

### BLOQUEJANT 4 — Eliminar DesktopPrefs.kt antiga de commonMain

**Fitxer a eliminar:** `shared/src/commonMain/kotlin/com/freelance/hores/util/DesktopPrefs.kt` (si existeix)

**Motiu:** Ara hi ha `AppPrefs` amb implementacions per plataforma a `androidMain` i `desktopMain`.

---

### BLOQUEJANT 5 — Corregir FitxarViewModel per usar AppPrefs

**Fitxer:** `shared/src/commonMain/kotlin/com/freelance/hores/ui/screen/fitxar/FitxarViewModel.kt`

**Problema:** Actualment no existeix al codebase. Cal crear-lo amb `AppPrefs` (no `SharedPreferences`).

**Crear el fitxer:**

```kotlin
package com.freelance.hores.ui.screen.fitxar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.di.AppPrefs
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import com.freelance.hores.ui.util.FormValidator
import com.freelance.hores.util.nowLocalTime
import com.freelance.hores.util.todayLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class FitxarViewModel(
    private val repository: RegistreRepository,
    private val prefs: AppPrefs
) : ViewModel() {

    private val _isFitxant = MutableStateFlow(prefs.getBoolean("is_fitxant", false))
    val isFitxant: StateFlow<Boolean> = _isFitxant.asStateFlow()

    private val _horaIniciArrodonida = MutableStateFlow(prefs.getString("hora_inici_arrodonida", ""))
    val horaIniciArrodonida: StateFlow<String> = _horaIniciArrodonida.asStateFlow()

    private val _dataFitxatge = MutableStateFlow(prefs.getString("data_fitxatge", ""))
    val dataFitxatge: StateFlow<String> = _dataFitxatge.asStateFlow()

    fun startFitxar() {
        val now = nowLocalTime()
        val rounded = FormValidator.roundToNearest15Minutes(now)
        val today = todayLocalDate()

        val roundedStr = formatTime(rounded)
        val todayStr = today.toString()

        prefs.putBoolean("is_fitxant", true)
        prefs.putString("hora_inici_arrodonida", roundedStr)
        prefs.putString("data_fitxatge", todayStr)

        _isFitxant.value = true
        _horaIniciArrodonida.value = roundedStr
        _dataFitxatge.value = todayStr
    }

    fun stopFitxar(onSuccess: (Long) -> Unit) {
        val startStr = _horaIniciArrodonida.value
        val dateStr = _dataFitxatge.value

        if (startStr.isEmpty() || dateStr.isEmpty()) return

        viewModelScope.launch {
            val today = LocalDate.parse(dateStr)
            val startLocalTime = parseTime(startStr)
            val endLocalTime = FormValidator.roundToNearest15Minutes(nowLocalTime())

            val existentDia = repository.getDiaByDate(today)
            val diaId = existentDia?.id ?: 0L

            val count = existentDia?.conceptes?.count { it.nom.startsWith("Bolo sense títol") } ?: 0
            val nouNom = "Bolo sense títol ${count + 1}"

            val nouConcepte = Concepte(
                diaId = diaId,
                nom = nouNom,
                preuHora = 0.0,
                estat = EstatFacturacio.PENDENT,
                rangsHoraris = listOf(
                    RangHorari(
                        concepteId = 0L,
                        horaInici = startLocalTime,
                        horaFi = endLocalTime
                    )
                )
            )

            val diaAGuardar = if (existentDia != null) {
                existentDia.copy(conceptes = existentDia.conceptes + nouConcepte)
            } else {
                Dia(data = today, conceptes = listOf(nouConcepte))
            }

            repository.saveDia(diaAGuardar)

            val diaGuardat = repository.getDiaByDate(today)
            val finalDiaId = diaGuardat?.id ?: 0L

            prefs.putBoolean("is_fitxant", false)
            prefs.putString("hora_inici_arrodonida", "")
            prefs.putString("data_fitxatge", "")

            _isFitxant.value = false
            _horaIniciArrodonida.value = ""
            _dataFitxatge.value = ""

            onSuccess(finalDiaId)
        }
    }

    private fun formatTime(time: LocalTime): String =
        "%02d:%02d".format(time.hour, time.minute)

    private fun parseTime(value: String): LocalTime {
        val parts = value.split(":")
        return LocalTime(parts[0].toInt(), parts[1].toInt())
    }
}
```

---

### BLOQUEJANT 6 — Crear ExportService multiplataforma

**Fitxers a crear/modificar:**

**A) Interfície comuna:** `shared/src/commonMain/kotlin/com/freelance/hores/data/export/ExportService.kt`

```kotlin
package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate

interface ExportService {
    fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate)
    fun exportPdf(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate)
}
```

**B) Implementació Android:** `shared/src/androidMain/kotlin/com/freelance/hores/data/export/ExportService.kt`

Renombrar la classe actual a `AndroidExportService` i fer-la implementar la interfície:

```kotlin
package com.freelance.hores.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate
import java.io.File

class AndroidExportService(private val context: Context) : ExportService {

    private val csvExporter = CsvExporter(context)
    private val pdfExporter = PdfExporter(context)

    override fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val filename = buildFilename("hores_freelance", "csv", startDate, endDate)
        val file = csvExporter.exportToCsv(dias, filename)
        shareFile(file, "text/csv")
    }

    override fun exportPdf(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
        val filename = buildFilename("hores_freelance", "pdf", startDate, endDate)
        val file = pdfExporter.exportToPdf(dias, filename, startDate, endDate)
        shareFile(file, "application/pdf")
    }

    private fun buildFilename(
        prefix: String,
        extension: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): String = "${prefix}_${startDate}_${endDate}.${extension}"

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, null)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
```

**C) Implementació Desktop:** `shared/src/desktopMain/kotlin/com/freelance/hores/data/export/DesktopExportService.kt`

Ja existeix al codebase. Verificar que implementa la interfície correctament:

```kotlin
package com.freelance.hores.data.export

import com.freelance.hores.domain.model.Dia
import kotlinx.datetime.LocalDate
import java.awt.Desktop
import java.io.File

class DesktopExportService : ExportService {

    override fun exportCsv(dias: List<Dia>, startDate: LocalDate, endDate: LocalDate) {
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
                    sb.appendLine(
                        "${dia.data},${concepte.nom},${concepte.preuHora},${rang.horaInici},${rang.horaFi},%.2f,%.2f,${concepte.despeses},${concepte.estat}"
                            .format(duracio, total)
                    )
                }
            }
        }
        return sb.toString()
    }
}
```

---

### BLOQUEJANT 7 — Corregir ResumViewModel per usar ExportService

**Fitxer:** `shared/src/commonMain/kotlin/com/freelance/hores/ui/screen/resum/ResumViewModel.kt`

**Problema:** Actualment no existeix al codebase mostrat. Cal crear-lo.

```kotlin
package com.freelance.hores.ui.screen.resum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.export.ExportService
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.util.lengthOfMonth
import com.freelance.hores.util.minusDays
import com.freelance.hores.util.plusDays
import com.freelance.hores.util.isoDayOfWeek
import com.freelance.hores.util.todayLocalDate
import com.freelance.hores.util.withDayOfMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class ResumState(
    val dias: List<Dia> = emptyList(),
    val clients: List<Client> = emptyList(),
    val startDate: LocalDate = todayLocalDate().minusDays(7),
    val endDate: LocalDate = todayLocalDate(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ResumViewModel(
    private val repository: RegistreRepository,
    private val exportService: ExportService
) : ViewModel() {
    private val _period = MutableStateFlow(Pair(todayLocalDate().minusDays(7), todayLocalDate()))
    private val _resumState = MutableStateFlow(ResumState())
    val resumState: StateFlow<ResumState> = _resumState.asStateFlow()

    init {
        observePeriod()
        observeClients()
        loadThisWeek()
    }

    private fun observeClients() {
        viewModelScope.launch {
            repository.getClients().collect { clients ->
                _resumState.value = _resumState.value.copy(clients = clients)
            }
        }
    }

    private fun observePeriod() {
        viewModelScope.launch {
            _period
                .flatMapLatest { (start, end) ->
                    repository.getDiasByDateRange(start, end)
                        .onStart {
                            _resumState.value = _resumState.value.copy(isLoading = true, error = null)
                        }
                        .catch { e ->
                            _resumState.value = _resumState.value.copy(
                                isLoading = false,
                                error = e.message ?: "An error occurred"
                            )
                        }
                }
                .collect { dias ->
                    _resumState.value = _resumState.value.copy(
                        dias = dias,
                        startDate = _period.value.first,
                        endDate = _period.value.second,
                        isLoading = false
                    )
                }
        }
    }

    fun loadThisWeek() {
        val today = todayLocalDate()
        val daysFromMonday = today.isoDayOfWeek() - 1
        val monday = today.minusDays(daysFromMonday.toLong())
        val sunday = monday.plusDays(6)
        _period.value = Pair(monday, sunday)
    }

    fun loadThisMonth() {
        val today = todayLocalDate()
        val firstDay = today.withDayOfMonth(1)
        val lastDay = today.withDayOfMonth(today.lengthOfMonth())
        _period.value = Pair(firstDay, lastDay)
    }

    fun loadLastMonth() {
        val today = todayLocalDate()
        val lastMonthLastDay = today.withDayOfMonth(1).minusDays(1)
        val lastMonthFirstDay = lastMonthLastDay.withDayOfMonth(1)
        _period.value = Pair(lastMonthFirstDay, lastMonthLastDay)
    }

    fun loadCustomPeriod(startDate: LocalDate, endDate: LocalDate) {
        _period.value = Pair(startDate, endDate)
    }

    fun getTotalHoras(): Double = _resumState.value.dias.sumOf { it.getTotalHoras() }

    fun getTotalDiners(): Double =
        _resumState.value.dias.sumOf { dia -> dia.conceptes.sumOf { it.getTotalDiners() } }

    fun getConceptesSummary(): Map<String, Double> {
        val summary = mutableMapOf<String, Double>()
        for (dia in _resumState.value.dias) {
            for (concepte in dia.conceptes) {
                summary[concepte.nom] = (summary[concepte.nom] ?: 0.0) + concepte.getTotalHoras()
            }
        }
        return summary.toSortedMap()
    }

    fun exportCsv(filteredDias: List<Dia>) {
        val state = _resumState.value
        exportService.exportCsv(filteredDias, state.startDate, state.endDate)
    }

    fun exportPdf(filteredDias: List<Dia>) {
        val state = _resumState.value
        exportService.exportPdf(filteredDias, state.startDate, state.endDate)
    }
}
```

---

### BLOQUEJANT 8 — Corregir imports fora de package

**Fitxer:** `shared/src/commonMain/kotlin/com/freelance/hores/data/repository/RegistreRepository.kt`

**Problema:** La primera línia és un `import` en lloc de `package`.

**Corregir:** Posar `package com.freelance.hores.data.repository` com a primera línia, i els imports després.

---

### BLOQUEJANT 9 — Migrar java.time a kotlinx-datetime

**Fitxers a modificar a `commonMain`:**

1. `domain/model/Dia.kt` — assegurar que usa `kotlinx.datetime.LocalDate`
2. `domain/model/RangHorari.kt` — assegurar que usa `kotlinx.datetime.LocalTime`
3. `data/repository/RegistreRepository.kt` — eliminar qualsevol `import java.time.*`
4. `ui/screen/*/` — tots els ViewModels i Screens

**Regla general:**
- `java.time.LocalDate` → `kotlinx.datetime.LocalDate`
- `java.time.LocalTime` → `kotlinx.datetime.LocalTime` (ja està a `DateUtils.kt`)
- `java.time.YearMonth` → usar la classe `YearMonth` de `DateUtils.kt`
- `java.time.format.DateTimeFormatter` → usar `.toString()` o helpers manuals
- `java.util.Locale` → eliminar on no sigui necessari

---

### BLOQUEJANT 10 — Corregir desktopApp/Main.kt per usar initKoin

**Fitxer:** `desktopApp/src/jvmMain/kotlin/com/freelance/hores/Main.kt`

**Problema:** Actualment no usa `initKoin`.

**Substituir la inicialització de Koin:**

```kotlin
// ELIMINAR:
remember { startKoin { modules(commonModule(), platformModule()) } }

// SUBSTITUIR per:
remember { initKoin() }
```

Afegir l'import: `import com.freelance.hores.di.initKoin`

---

### BLOQUEJANT 11 — Netejar imports residuals de Hilt/Dagger

**Revisar TOTS els fitxers de `commonMain` i `desktopMain`:**
- Eliminar `import dagger.hilt.android.lifecycle.HiltViewModel`
- Eliminar `import dagger.hilt.android.AndroidEntryPoint`
- Eliminar `@HiltViewModel` i `@HiltAndroidApp`
- Eliminar `import javax.inject.Inject` i `@Inject`
- Eliminar `import org.koin.core.annotation.KoinExperimentalAPI` si no s'usa

---

## PROBLEMES ADDICIONALS (NO BLOQUEJANTS PERÒ NECESSARIS)

### PROBLEMA A — Substituir strings "Valor" hardcoded

**Afecta:** Dialog.kt, DiaDetallScreen.kt, RegistreScreen.kt, ResumScreen.kt

Hi ha **32 ocurrències** de `"Valor"` com a placeholder de textos de UI. Substituir per strings reals:
- `"Valor"` → textos descriptius segons el context ("Confirmar", "Desa", "Cancel·la", etc.)
- O millor: usar recursos de strings (però com que és KMP, de moment hardcodejar en català correctament)

### PROBLEMA B — Corregir navegació a registre

**Fitxer:** `DiaDetallScreen.kt`

La navegació `navController.navigate("registre?diaId=${localDia.id}")` falta el paràmetre `data`.

**Corregir:**
```kotlin
navController.navigate("registre?diaId=${localDia.id}&data=${localDia.data}")
```

### PROBLEMA C — Afegir imports faltants de TimePicker

**Fitxer:** `RegistreScreen.kt`

Afegir:
```kotlin
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker
```

### PROBLEMA D — Corregir FormValidator

**Fitxer:** `shared/src/commonMain/kotlin/com/freelance/hores/ui/util/FormValidator.kt`

**Problemes:**
1. `validateTimeRange` sempre retorna `Success` (no implementat)
2. `validateConceptesCount` té missatge d'error incorrecte (diu "El nom del bolo no pot estar buit" en lloc de demanar almenys un concepte)

**Implementar:**

```kotlin
fun validateTimeRange(startTime: LocalTime, endTime: LocalTime): ValidationResult {
    val startSeconds = startTime.totalSecondsFromMidnight()
    val endSeconds = endTime.totalSecondsFromMidnight()
    return if (endSeconds <= startSeconds) {
        ValidationResult.Error("L'hora de fi ha de ser posterior a l'inici")
    } else {
        ValidationResult.Success
    }
}
```

### PROBLEMA E — Millorar rendiment de RegistreRepository

**Fitxer:** `RegistreRepository.kt`

**Problema:** `getConceptesForDia` fa N+1 queries (un `getRangsForConcepte` per cada concepte).

**Solució provisional:** Afegir `@Transaction` o usar `withTransaction` per a `saveDia`:

```kotlin
import androidx.room.withTransaction

// A saveDia():
suspend fun saveDia(dia: Dia): Long = database.withTransaction {
    // ... lògica actual ...
}
```

### PROBLEMA F — Corregir CalendariViewModel.loadDias()

**Fitxer:** `CalendariViewModel.kt`

**Problema:** `loadDias()` fa `_currentMonth.value = _currentMonth.value` (no-op).

**Corregir:**

```kotlin
fun loadDias() {
    // Forçar recàrrega emetent el mateix valor
    _currentMonth.value = _currentMonth.value
    // O millor: cridar directament el repositori
}
```

---

## Ordre d'execució recomanat

1. **BLOQUEJANT 1** — `@ConstructedBy` (sense això no compila res)
2. **BLOQUEJANT 2** — Dependències Koin al build.gradle.kts
3. **BLOQUEJANT 3** — Corregir Koin.kt (viewModel DSL)
4. **BLOQUEJANT 4** — Eliminar DesktopPrefs antiga
5. **BLOQUEJANT 5** — Crear FitxarViewModel
6. **BLOQUEJANT 6** — ExportService multiplataforma
7. **BLOQUEJANT 7** — Crear ResumViewModel
8. **BLOQUEJANT 8** — Corregir import fora de package
9. **BLOQUEJANT 9** — Migrar java.time a kotlinx-datetime
10. **BLOQUEJANT 10** — Main.kt desktop
11. **BLOQUEJANT 11** — Netejar imports residuals
12. **PROBLEMES A-F** — Millores addicionals

---

## Verificació final

Un cop aplicats tots els canvis, confirmar que:
- [ ] `./gradlew :shared:compileCommonMainKotlinMetadata` passa sense errors
- [ ] `./gradlew :androidApp:assembleDebug` passa sense errors
- [ ] `./gradlew :desktopApp:run` arranca sense crash
- [ ] Cap fitxer de `commonMain` conté `import android.*`
- [ ] Cap fitxer de `commonMain` conté `android.content.Intent` ni `LocalContext`
- [ ] Cap fitxer de `commonMain` o `desktopMain` conté `SharedPreferences`
- [ ] `commonModule()` registra tots els ViewModels amb `viewModel { }`
- [ ] Tots els strings "Valor" han estat substituïts per textos reals
