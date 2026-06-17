Excel·lent decisió. Prescindir del temporitzador en moviment és una opció que evita codi innecessari de refresc en segon pla (corutines/handlers) i fa que la pantalla sigui completament estàtica i neta, mostrant només la informació útil. 

A continuació, tens tota la guia i el codi preparat de manera clara i estructurada perquè tu o l'agent de codi pugueu aplicar exactament les modificacions fitxer per fitxer.

---

### 📂 Modificacions i Creacions pas a pas

#### 1. 🆕 Nou fitxer: `FormValidator.kt` (Afegir la funció d'arrodoniment)
Afegim la funció de càlcul d'arrodoniment de temps a quarts d'hora al fitxer existent de validació.

**Com canviar a `com/freelance/hores/ui/util/FormValidator.kt`:**
```kotlin
package com.freelance.hores.ui.util

import java.time.LocalTime
import com.freelance.hores.R

object FormValidator {
    
    // ... manté les funcions de validació actuals intactes ...

    /**
     * Arrodoneix un LocalTime al quart d'hora més proper (00, 15, 30, 45)
     */
    fun roundToNearest15Minutes(time: LocalTime): LocalTime {
        val minutes = time.minute
        val roundedMinutes = ((minutes + 7) / 15) * 15
        return if (roundedMinutes == 60) {
            time.plusHours(1).withMinute(0).withSecond(0).withNano(0)
        } else {
            time.withMinute(roundedMinutes).withSecond(0).withNano(0)
        }
    }
}

// ... manté el 'sealed class ValidationResult' actual ...
```

---

#### 2. 🆕 Nou fitxer: `TimePicker15MinDialog.kt` (Opció B del Selector)
Creem un diàleg customitzat per complir amb l'**Opció B**. En lloc de fer servir el diàleg de rellotge estàndard, farem servir un diàleg amb selectors tancats on l'usuari només pot escollir minuts del conjunt `[00, 15, 30, 45]`.

**Crear a `com/freelance/hores/ui/component/TimePicker15MinDialog.kt`:**
```kotlin
package com.freelance.hores.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker15MinDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember {
        val currentMin = initialTime.minute
        val rounded = ((currentMin + 7) / 15) * 15
        if (rounded == 60) 0 else rounded
    }

    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona l'hora") },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector d'hores (00 a 23)
                ExposedDropdownMenuBox(
                    expanded = hourExpanded,
                    onExpandedChange = { hourExpanded = !hourExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "%02d".format(selectedHour),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hourExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = hourExpanded,
                        onDismissRequest = { hourExpanded = false }
                    ) {
                        (0..23).forEach { hour ->
                            DropdownMenuItem(
                                text = { Text("%02d".format(hour)) },
                                onClick = {
                                    selectedHour = hour
                                    hourExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(":", style = MaterialTheme.typography.headlineMedium)

                // Selector de minuts restrictiu de 15 en 15 (00, 15, 30, 45)
                ExposedDropdownMenuBox(
                    expanded = minuteExpanded,
                    onExpandedChange = { minuteExpanded = !minuteExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "%02d".format(selectedMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Minuts") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = minuteExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = minuteExpanded,
                        onDismissRequest = { minuteExpanded = false }
                    ) {
                        listOf(0, 15, 30, 45).forEach { minute ->
                            DropdownMenuItem(
                                text = { Text("%02d".format(minute)) },
                                onClick = {
                                    selectedMinute = minute
                                    minuteExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime.of(selectedHour, selectedMinute))
                }
            ) {
                Text("Desa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la")
            }
        }
    )
}
```

---

#### 3. 🆕 Nou fitxer: `FitxarViewModel.kt` (La gestió d'estat a SharedPreferences)
Aquest ViewModel s'encarrega d'emmagatzemar l'estat ràpid amb `SharedPreferences`. En polsar **STOP**, es crearà automàticament un Bolo en blanc (Bolo sense títol) i s'enviarà l'ID de forma immediata.

**Crear a `com/freelance/hores/ui/screen/fitxar/FitxarViewModel.kt`:**
```kotlin
package com.freelance.hores.ui.screen.fitxar

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.data.repository.RegistreRepository
import com.freelance.hores.domain.model.Concepte
import com.freelance.hores.domain.model.Dia
import com.freelance.hores.domain.model.RangHorari
import com.freelance.hores.ui.util.FormValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FitxarViewModel @Inject constructor(
    private val repository: RegistreRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _isFitxant = MutableStateFlow(prefs.getBoolean("is_fitxant", false))
    val isFitxant: StateFlow<Boolean> = _isFitxant.asStateFlow()

    private val _horaIniciArrodonida = MutableStateFlow(prefs.getString("hora_inici_arrodonida", ""))
    val horaIniciArrodonida: StateFlow<String> = _horaIniciArrodonida.asStateFlow()

    private val _dataFitxatge = MutableStateFlow(prefs.getString("data_fitxatge", ""))
    val dataFitxatge: StateFlow<String> = _dataFitxatge.asStateFlow()

    fun startFitxar() {
        val now = LocalTime.now()
        val rounded = FormValidator.roundToNearest15Minutes(now)
        val today = LocalDate.now()

        val roundedStr = rounded.format(DateTimeFormatter.ofPattern("HH:mm"))
        val todayStr = today.toString()

        prefs.edit().apply {
            putBoolean("is_fitxant", true)
            putString("hora_inici_arrodonida", roundedStr)
            putString("data_fitxatge", todayStr)
            apply()
        }

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
            val startLocalTime = LocalTime.parse(startStr)
            val endLocalTime = FormValidator.roundToNearest15Minutes(LocalTime.now())

            // Recupera o crea el Dia per a la data desada
            val existentDia = repository.getDiaByDate(today)
            val diaId = existentDia?.id ?: 0L

            val nouConcepte = Concepte(
                diaId = diaId,
                nom = "Bolo sense títol",
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
                Dia(
                    data = today,
                    conceptes = listOf(nouConcepte)
                )
            }

            repository.saveDia(diaAGuardar)

            // Obtenim l'ID definitiu de la DB
            val diaGuardat = repository.getDiaByDate(today)
            val finalDiaId = diaGuardat?.id ?: 0L

            // Esborrem dades temporals
            prefs.edit().apply {
                putBoolean("is_fitxant", false)
                putString("hora_inici_arrodonida", "")
                putString("data_fitxatge", "")
                apply()
            }

            _isFitxant.value = false
            _horaIniciArrodonida.value = ""
            _dataFitxatge.value = ""

            onSuccess(finalDiaId)
        }
    }
}
```

---

#### 4. 🆕 Nou fitxer: `FitxarScreen.kt` (Pantalla Principal estàtica)
Aquí mostrem el gran botó verd (START) o vermell (STOP), acompanyat de la data actual i la informació del fitxatge en el moment en què s'ha premut el START (sense consums de bateria ni comptadors actius).

**Crear a `com/freelance/hores/ui/screen/fitxar/FitxarScreen.kt`:**
```kotlin
package com.freelance.hores.ui.screen.fitxar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitxarScreen(
    navController: NavHostController,
    viewModel: FitxarViewModel = hiltViewModel()
) {
    val isFitxant by viewModel.isFitxant.collectAsState()
    val horaIniciArrodonida by viewModel.horaIniciArrodonida.collectAsState()

    val formattedDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fitxar ràpid") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isFitxant) "Sessió de fitxatge en curs" else "A punt per començar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Gran botó circular
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        color = if (isFitxant) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                        shape = CircleShape
                    )
                    .clickable {
                        if (isFitxant) {
                            viewModel.stopFitxar { diaId ->
                                navController.navigate("registre?diaId=$diaId")
                            }
                        } else {
                            viewModel.startFitxar()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFitxant) "STOP" else "START",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Informació d'estat a sota (sense temporitzador actiu)
            if (isFitxant && horaIniciArrodonida.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Fitxant actualment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Hora d'inici (arrodonida): $horaIniciArrodonida",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Text(
                    text = "En prémer START es desarà l'hora d'inici arrodonida al quart d'hora més proper.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
```

---

#### 5. 🛠️ Modificar fitxer: `DatabaseModule.kt` (Proporcionar SharedPreferences via Hilt)
Necessitem injectar SharedPreferences a `FitxarViewModel`. Per fer-ho, afegim el proveïdor de forma neta a `DatabaseModule`.

**Com modificar a `com/freelance/hores/di/DatabaseModule.kt`:**
```kotlin
package com.freelance.hores.di

import android.content.Context
import com.freelance.hores.data.db.AppDatabase
// ... resta d'imports ...
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    // ... manté provideAppDatabase, provideDiaDao, etc ...

    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): android.content.SharedPreferences {
        return context.getSharedPreferences("hores_prefs", Context.MODE_PRIVATE)
    }
}
```

---

#### 6. 🛠️ Modificar fitxer: `Screen.kt` (Integrar Fitxar com a primera pestanya)
Afegim la pantalla de Fitxar a la llista d'elements de la barra de navegació inferior.

**Com modificar a `com/freelance/hores/ui/navigation/Screen.kt`:**
```kotlin
package com.freelance.hores.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Fitxar : Screen("fitxar", "Fitxar", Icons.Default.PlayArrow) // Afegit
    object Calendari : Screen("calendari", "Calendari", Icons.Default.DateRange)
    object Resum : Screen("resum", "Resum", Icons.Default.List)
    object Clients : Screen("clients", "Clients", Icons.Default.Person)
}

val bottomNavScreens = listOf(
    Screen.Fitxar, // Afegit com a primer tab
    Screen.Calendari,
    Screen.Resum,
    Screen.Clients
)
```

---

#### 7. 🛠️ Modificar fitxer: `AppNavHost.kt` (Afegir FitxarScreen i fer-la startDestination)
Fem que l'aplicació iniciï per defecte a la pantalla de fitxar ràpid.

**Com modificar a `com/freelance/hores/ui/navigation/AppNavHost.kt`:**
```kotlin
package com.freelance.hores.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freelance.hores.ui.screen.calendari.CalendariScreen
import com.freelance.hores.ui.screen.clients.ClientsScreen
import com.freelance.hores.ui.screen.dia.DiaDetallScreen
import com.freelance.hores.ui.screen.fitxar.FitxarScreen // Importat
import com.freelance.hores.ui.screen.registre.RegistreScreen
import com.freelance.hores.ui.screen.resum.ResumScreen
import java.time.LocalDate

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "fitxar" // Definit com startDestination
    ) {
        composable("fitxar") {
            FitxarScreen(navController = navController)
        }

        composable("calendari") {
            CalendariScreen(navController = navController)
        }
        
        // ... la resta de rutes (dia, registre, resum, clients) es queden exactament igual ...
    }
}
```

---

#### 8. 🛠️ Modificar fitxer: `CalendariScreen.kt` (Ressaltar el dia actual en el calendari)
Modifiquem el component de cel·la de dia (`DayCell`) i la reixa del calendari (`CalendarGrid`) per ressaltar visualment la data d'avui amb un contorn/vora primari de 2dp.

**Com modificar a `com/freelance/hores/ui/screen/calendari/CalendariScreen.kt`:**

Busca el mètode `CalendarGrid` i actualitza-lo perquè envieu la comprovació `isToday`:
```kotlin
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    diasWithRecords: List<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    // ... manté la capçalera de dies igual ...

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(totalCells) { index ->
            val dayOfMonth = index - emptyCellsBefore + 1
            if (dayOfMonth in 1..daysInMonth) {
                val date = yearMonth.atDay(dayOfMonth)
                val hasRecord = date in diasWithRecords
                val isToday = date == LocalDate.now() // Calcula si el dia de la cel·la és avui
                DayCell(
                    day = dayOfMonth,
                    hasRecord = hasRecord,
                    isToday = isToday, // Envia el nou paràmetre
                    onClick = { onDateClick(date) }
                )
            } else {
                Box(modifier = Modifier.size(48.dp))
            }
        }
    }
}
```

I actualitza la funció `DayCell` perquè dibuixi el contorn de color d'avui:
```kotlin
@Composable
fun DayCell(
    day: Int,
    hasRecord: Boolean,
    isToday: Boolean, // Afegit
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (hasRecord) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .then(
                if (isToday) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary, // Color del cercle/vora del dia actual
                        shape = MaterialTheme.shapes.small
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (hasRecord) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}
```

---

#### 9. 🛠️ Modificar fitxer: `RegistreScreen.kt` (Integració de l'Opció B de selecció horària)
Canviem el selector de temps del formulari manual perquè faci servir el nostre nou component `TimePicker15MinDialog` en lloc dels selectors natius de rellotge i minuts solts.

**Com modificar a `com/freelance/hores/ui/screen/registre/RegistreScreen.kt`:**

Primer afegeix l'import del nostre nou component a la capçalera de `RegistreScreen.kt`:
```kotlin
import com.freelance.hores.ui.component.TimePicker15MinDialog
```

I a la funció `RangHorariFormItem` d'aquest mateix fitxer, localitza on es controlen `showStartPicker` i `showEndPicker`, i canvia el diàleg existent per aquest codi:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangHorariFormItem(
    rang: RangHorariForm,
    onUpdateInici: (LocalTime) -> Unit,
    onUpdateFi: (LocalTime) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    if (showStartPicker) {
        TimePicker15MinDialog(
            initialTime = rang.horaInici,
            onDismiss = { showStartPicker = false },
            onConfirm = { horaSeleccionada ->
                onUpdateInici(horaSeleccionada)
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        TimePicker15MinDialog(
            initialTime = rang.horaFi,
            onDismiss = { showEndPicker = false },
            onConfirm = { horaSeleccionada ->
                onUpdateFi(horaSeleccionada)
                showEndPicker = false
            }
        )
    }

    // ... manté la resta del codi de RangHorariFormItem (la visualització de la targeta amb els botons) completament igual ...
}
```

---

L'agent de codi ja disposa amb aquesta estructura de tota la informació necessària per aplicar la solució amb èxit. Com que hem integrat i respectat tota l'arquitectura existent (Hilt, Compose Material 3, repositoris i SharedPreferences), tota l'app estarà harmònica.
