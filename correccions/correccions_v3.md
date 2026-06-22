# Correccions v3 — Errors al prompt_agent_correccions.md

Aquest document corregeix **4 errors** detectats al fitxer
`prompt_agent_correccions.md` del repositori abans de passar-lo a l'agent.

---

## CORRECCIÓ 1 — BLOQUEJANT 10: `initKoin()` no pot anar dins d'un `remember`

**On:** Secció "BLOQUEJANT 10 — Corregir desktopApp/Main.kt per usar initKoin"

**Problema:** El prompt indica substituir `remember { startKoin { ... } }` per
`remember { initKoin() }`. Però `remember { }` s'executa dins la composició
de Compose, i Koin ha d'inicialitzar-se **una sola vegada a l'inici de
l'aplicació**, fora de qualsevol composable. Posar-ho dins d'un `remember`
podria reinicialitzar Koin en recomposicions i causa errors impredictibles.

**Substituir la instrucció del BLOQUEJANT 10 per:**

```kotlin
// desktopApp/src/jvmMain/kotlin/com/freelance/hores/Main.kt

import com.freelance.hores.di.initKoin
import androidx.compose.ui.window.application

fun main() = application {
    initKoin()   // ← aquí, fora de qualsevol composable, s'executa una sola vegada
    Window(
        onCloseRequest = ::exitApplication,
        title = "HoresFreelance"
    ) {
        App()
    }
}
```

**Regla:** `initKoin()` sempre al `main()`, mai dins de `remember`, `LaunchedEffect`
ni cap composable.

---

## CORRECCIÓ 2 — PROBLEMA E: aclarir que `withTransaction` SÍ és vàlid al `commonMain`

**On:** Secció "PROBLEMA E — Millorar rendiment de RegistreRepository"

**Problema:** El BLOQUEJANT 8 del mateix document demana corregir un `import`
que anava **abans del `package`** al `RegistreRepository.kt`. Això pot crear
confusió i fer que l'agent elimini `withTransaction` pensant que és el problema,
quan en realitat `import androidx.room.withTransaction` és perfectament vàlid
al `commonMain` — l'únic error era l'ordre.

**Afegir aquesta nota al PROBLEMA E, just abans del codi:**

> **Nota important:** `import androidx.room.withTransaction` és compatible amb
> `commonMain` en Room KMP 2.7.0-alpha01. L'error documentat al BLOQUEJANT 8
> **no era l'import en si**, sinó que apareixia *abans* de la declaració
> `package`. Un cop el BLOQUEJANT 8 estigui aplicat (package primer, imports
> després), `withTransaction` funcionarà correctament. No cal eliminar-lo.

**El codi del PROBLEMA E queda així (sense canvis al codi, només cal aplicar-lo
després del BLOQUEJANT 8):**

```kotlin
package com.freelance.hores.data.repository  // ← sempre primer

import androidx.room.withTransaction           // ← ara sí, correcte

// ...

suspend fun saveDia(dia: Dia): Long = database.withTransaction {
    // ... lògica actual ...
}
```

---

## CORRECCIÓ 3 — BLOQUEJANT 5: `diaId` incorrecte al `FitxarViewModel.stopFitxar()`

**On:** Secció "BLOQUEJANT 5 — Corregir FitxarViewModel per usar AppPrefs",
dins la funció `stopFitxar()`

**Problema:** El codi proposa:

```kotlin
val diaId = existentDia?.id ?: 0L

val nouConcepte = Concepte(
    diaId = diaId,   // ← problema: si existentDia == null, diaId és 0L
    ...
)

val diaAGuardar = if (existentDia != null) {
    existentDia.copy(conceptes = existentDia.conceptes + nouConcepte)
} else {
    Dia(data = today, conceptes = listOf(nouConcepte))  // dia nou amb diaId = 0L
}

repository.saveDia(diaAGuardar)  // ← guarda i assigna l'ID real a la BD
```

Quan és un dia nou, el `nouConcepte.diaId = 0L` s'insereix a la base de dades
amb un ID invàlid. El repositori hauria de reassignar-lo dins de `saveDia`,
però si no ho fa explícitament, el `ConcepteEntity.diaId` quedarà a `0`
i la relació s'haurà perdut.

**Substituir el bloc afectat del `stopFitxar()` per:**

```kotlin
fun stopFitxar(onSuccess: (Long) -> Unit) {
    val startStr = _horaIniciArrodonida.value
    val dateStr = _dataFitxatge.value

    if (startStr.isEmpty() || dateStr.isEmpty()) return

    viewModelScope.launch {
        val today = LocalDate.parse(dateStr)
        val startLocalTime = parseTime(startStr)
        val endLocalTime = FormValidator.roundToNearest15Minutes(nowLocalTime())

        val existentDia = repository.getDiaByDate(today)

        val count = existentDia?.conceptes?.count {
            it.nom.startsWith("Bolo sense títol")
        } ?: 0
        val nouNom = "Bolo sense títol ${count + 1}"

        // diaId = 0L sempre: el repositori l'assigna correctament dins saveDia()
        // tant si és dia nou (INSERT) com si ja existeix (UPDATE amb l'ID real)
        val nouConcepte = Concepte(
            diaId = 0L,
            nom = nouNom,
            preuHora = 0.0,
            despeses = 0.0,
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
            // Dia existent: el diaId del concepte l'assignarà saveDia()
            // quan faci l'INSERT del ConcepteEntity amb el diaId correcte
            existentDia.copy(conceptes = existentDia.conceptes + nouConcepte)
        } else {
            Dia(data = today, conceptes = listOf(nouConcepte))
        }

        repository.saveDia(diaAGuardar)

        // Llegir l'ID real del dia un cop guardat
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
```

**Requisit:** Verificar que `RegistreRepository.saveDia()` assigna correctament
el `diaId` als conceptes quan fa l'`INSERT`. Ha de fer quelcom com:

```kotlin
val diaEntityId = diaDao.insertDia(diaEntity)  // retorna l'ID generat
for (concepte in dia.conceptes) {
    val concepteEntityId = concepteDao.insertConcepte(
        concepte.toEntity(diaId = diaEntityId)  // ← usa l'ID real
    )
    // ...
}
```

Si `saveDia()` ja fa això correctament, el canvi de `diaId = existentDia?.id ?: 0L`
a `diaId = 0L` al ViewModel no trenca res — simplement evita una inconsistència
temporal innecessària.

---

## CORRECCIÓ 4 — PROBLEMA F: la solució de `loadDias()` no funciona

**On:** Secció "PROBLEMA F — Corregir CalendariViewModel.loadDias()"

**Problema:** El prompt detecta correctament que
`_currentMonth.value = _currentMonth.value` és un no-op (no emet cap nou valor
perquè `StateFlow` descarta emissions iguals), però la "solució" proposada
és exactament el mateix no-op amb un comentari diferent.

**Substituir la instrucció del PROBLEMA F per una d'aquestes dues opcions:**

**Opció A — Solució mínima (si les queries del repositori ja són `Flow`):**

Si `CalendariViewModel` observa el mes actual amb un `flatMapLatest` o similar,
les dades ja s'actualitzen automàticament quan el repositori emet canvis.
En aquest cas, `loadDias()` simplement no cal i s'ha d'eliminar o deixar buida:

```kotlin
// Si les queries del repositori ja retornen Flow i s'observen al init{},
// eliminar loadDias() o deixar-la buida. Les dades s'actualitzen soles.
fun loadDias() { /* no-op intencional: les dades venen de Flow del repositori */ }
```

**Opció B — Solució si cal forçar una recàrrega manual:**

Afegir un `MutableSharedFlow` de refresc separat:

```kotlin
private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0)

init {
    viewModelScope.launch {
        combine(_currentMonth, _refreshTrigger.onStart { emit(Unit) }) { month, _ -> month }
            .flatMapLatest { month -> repository.getDiasByMonth(month) }
            .collect { dias -> _dias.value = dias }
    }
}

fun loadDias() {
    viewModelScope.launch {
        _refreshTrigger.emit(Unit)  // força una nova emissió sense canviar el mes
    }
}
```

**Recomanació:** Mirar primer com està implementat l'`init { }` del
`CalendariViewModel` actual. Si ja observa un `Flow` del repositori,
usar l'Opció A. Si no, usar l'Opció B.

---

## Ordre d'aplicació d'aquestes correccions

Aplicar **abans** de passar el `prompt_agent_correccions.md` a l'agent:

1. Editar el BLOQUEJANT 10 amb el codi de la Correcció 1
2. Afegir la nota aclaridora al PROBLEMA E (Correcció 2)
3. Substituir el bloc `stopFitxar()` al BLOQUEJANT 5 (Correcció 3)
4. Substituir la instrucció del PROBLEMA F (Correcció 4)

La resta del document és correcte i es pot passar a l'agent tal com està.
