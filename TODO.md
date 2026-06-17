

**Missió:** Implementar la nova funcionalitat de "Fitxar ràpid" (Clock-in/out), solucionar tres errors crítics de comportament identificats en l'última revisió de codi, i aplicar optimitzacions clau en la base de dades i en la gestió de la mitjanit.

---

### ⚠️ IMPORTANT: PROTOCOL D'INICIALITZACIÓ OBLIGATORI
Abans d'escriure qualsevol línia de codi o fer modificacions, has de:
1. Crear un fitxer de planificació de tasques a `~/asi/temp/task.md` (no utilitzis el directori de treball actual per a fitxers temporals).
2. Definir una matriu de validació (Constraints Matrix) amb proves PASS/FAIL per a cada modificació d'aquest prompt.
3. Compilar el codi utilitzant `blaze build` (per exemple, per a Android `blaze build //app/...` o similar segons correspongui) fent servir un base de compilació aïllat (`--output_base=/tmp/blaze_build_agent`) per evitar col·lisions d'escriptura de fitxers temporals.

---

### 📂 FITXERS A MODIFICAR O CREAR

#### 1. Lògica del creuament de Mitjanit
*   **Fitxer:** `com/freelance/hores/domain/model/RangHorari.kt`
    *   **Acció:** Modifica `getDuracionaEnHoras()` per detectar si l'hora de fi és anterior a la de l'inici (creuament de mitjanit). Si és així, afegeix un dia sencer (24 hores) als segons totals de durada per evitar hores negatives.
*   **Fitxer:** `com/freelance/hores/ui/util/FormValidator.kt`
    *   **Acció:** Actualitza el mètode `validateTimeRange` perquè permeti que l'hora de fi sigui inferior a la d'inici en cas que s'hagi marcat o s'entengui com a canvi de dia.

#### 2. Optimització del flux de Base de Dades (ID del Dia)
*   **Fitxer:** `com/freelance/hores/data/repository/RegistreRepository.kt`
    *   **Acció:** Modifica la firma de `suspend fun saveDia(dia: Dia)` perquè retorni un `Long` (l'ID del dia generat o actualitzat). Revisa com Room retorna l'ID a `diaDao.insert` i retorna'l correctament a través de la transacció.

#### 3. Persistència i flux de "Fitxar ràpid"
*   **Fitxer:** `com/freelance/hores/di/DatabaseModule.kt`
    *   **Acció:** Si no s'ha fet encara, proporciona SharedPreferences via Hilt amb un nom de fitxer de preferències privat (`"hores_prefs"`).
*   **Fitxer:** `com/freelance/hores/ui/screen/fitxar/FitxarViewModel.kt` (Nou / Completat)
    *   **Acció:** Implementa la gestió del fitxatge llegint/escrivint de `SharedPreferences`. En fer STOP, s'ha de cridar `repository.saveDia`, rebre l'ID de retorn a l'instant gràcies a l'optimització anterior, i invocar el callback `onSuccess(diaId)` cap a la pantalla de registre.
*   **Fitxer:** `com/freelance/hores/ui/screen/fitxar/FitxarScreen.kt` (Nou / Completat)
    *   **Acció:** Interfície estàtica i neta. Un botó verd circular gran (START) quan està inactiu que desa l'hora arrodonida inicial. Quan està actiu, es converteix en un botó vermell circular (STOP) i mostra una targeta fixa d'informació: *"Fitxant actualment des de les [Hora d'inici arrodonida]"*. No posis cap temporitzador de segons que s'actualitzi constantment per tal de mantenir un consum d'energia òptim.

#### 4. Resoldre el Crash en esborrar un Bolo
*   **Fitxer:** `com/freelance/hores/ui/screen/dia/DiaDetallScreen.kt`
    *   **Acció:** 
        1. Protegeix l'estat durant recomposicions utilitzant una variable local immutable: `val localDia = dia`.
        2. Assigna una clau única al llistat de l'historial a Compose: `items(localDia.conceptes, key = { it.id }) { ... }` per evitar que recomposicions de llistes dinàmiques generin un crash amb pèrdua d'índexs a la UI.

#### 5. Resoldre la pèrdua de Clients al fitxar
*   **Fitxer:** `com/freelance/hores/ui/screen/registre/RegistreViewModel.kt`
    *   **Acció:** Modifica el mètode `loadDiaForEditing(diaId)` perquè actualitzi l'estat utilitzant `_formState.value = _formState.value.copy(...)` en lloc d'instanciar un `RegistreFormState` des de zero. Això evitarà que s'esborri la llista de `clients` carregada al `init`.

#### 6. Ressaltar el Dia Actual al Calendari
*   **Fitxer:** `com/freelance/hores/ui/screen/calendari/CalendariScreen.kt`
    *   **Acció:** 
        1. Modifica `CalendarGrid` per calcular per a cada cel·la `val isToday = date == LocalDate.now()`.
        2. Passa `isToday` a la funció `DayCell` com a paràmetre booleà.
        3. A `DayCell`, afegeix un `.border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)` i fes que el text estigui en `FontWeight.Bold` si és el dia d'avui.

#### 7. Restringir a quarts d'hora els selectors de l'app (Opció B)
*   **Fitxer:** `com/freelance/hores/ui/component/TimePicker15MinDialog.kt` (Nou)
    *   **Acció:** Desenvolupa un diàleg customitzat d'interfície que limiti la tria de minuts en una llista tancada: `[00, 15, 30, 45]` fent servir desplegables de Material 3.
*   **Fitxer:** `com/freelance/hores/ui/screen/registre/RegistreScreen.kt`
    *   **Acció:** Reemplaça el diàleg estàndard de Compose `TimePicker` i `rememberTimePickerState` dels mètodes `showStartPicker` i `showEndPicker` per aquest nou diàleg `TimePicker15MinDialog`.

#### 8. Rutes i Navegació
*   **Fitxer:** `com/freelance/hores/ui/navigation/AppNavHost.kt`
    *   Assegura't que el punt d'entrada per defecte (`startDestination`) estigui fixat a `"fitxar"`.
*   **Fitxer `com/freelance/hores/ui/navigation/Screen.kt`**:
    *   Verifica que la pantalla `Fitxar` estigui ben declarada a `bottomNavScreens` i situada en primera posició.

---

### 🧪 Prova de tancament obligatòria (Pre-Flight)
Un cop fets els canvis, valida que:
1. L'aplicació compili correctament (`./gradlew assembleDebug`).
2. Des de la pantalla d'inici, pots prémer START, canviar de pantalla o tancar l'app, tornar-la a obrir, prémer STOP, i seleccionar directament qualsevol client preexistent al formulari de registre sense que la llista surti buida.
3. Si esborres qualsevol bolo des de la pantalla de detall del dia, la llista es refresca visualment sense tancar l'app de cop.
4. El dia d'avui està marcat amb una vora blava ben visible en obrir el Calendari.