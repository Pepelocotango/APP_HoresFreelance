
>
> **Entorn de desenvolupament:** Ubuntu MATE 24.04 LTS (Noble Numbat) · 64 bits

---

## ROL I CONTEXT

Ets un expert en desenvolupament Android natiu amb Kotlin i Jetpack Compose.
Has de crear una aplicació Android completa per a un professional freelance que necessita
registrar els seus dies de treball i els horaris associats, per poder generar posteriorment
les seves factures en una eina externa.

L'app és per a ús personal, funciona completament offline, sense cap backend ni autenticació.

---

## OBJECTIU DE L'APP

Crear una app Android nativa anomenada **"HoresFreelance"** (o "Time Tracker Freelance")
que permeti:

1. Registrar **dies de treball** amb data i notes opcionals.
2. Per a cada dia, registrar un o més **conceptes** (el nom del projecte, client o tasca).
3. Per a cada concepte, registrar un o més **rangs horaris** (hora d'inici i hora de fi).
4. Consultar un **resum** de les hores treballades per període (setmana, mes, rang personalitzat).
5. **Exportar** les dades (CSV i PDF) per facilitar la creació de factures en eines externes.

---

## STACK TECNOLÒGIC (obligatori, no canviar)

- **Llenguatge:** Kotlin 1.9+
- **UI:** Jetpack Compose (Material Design 3)
- **Base de dades local:** Room 2.6+
- **Arquitectura:** MVVM + StateFlow + Repository pattern
- **Navegació:** Navigation Compose
- **Injecció de dependències:** Hilt
- **Build:** Gradle (Kotlin DSL, fitxers `.kts`)
- **CI/CD:** GitHub Actions
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35
- **Control de versions:** Git + GitHub

---

## ENTORN DE DESENVOLUPAMENT (Ubuntu MATE 24.04 LTS)

L'entorn on es desenvolupa i compila l'app és **Ubuntu MATE 24.04 LTS (Noble Numbat) 64 bits**.
Tot el codi, les instruccions de terminal i els scripts han de ser compatibles amb aquest sistema.

### Prerequisits del sistema — instal·lació

Executar en terminal abans de començar:

```bash
# 1. Actualitzar el sistema
sudo apt update && sudo apt upgrade -y

# 2. Java 17 (requerit per Android Gradle Plugin 8.x)
sudo apt install -y openjdk-17-jdk
java -version   # ha de mostrar openjdk 17

# 3. Dependències de sistema per a Android Studio i l'emulador
sudo apt install -y \
  git curl wget unzip zip \
  libglu1-mesa libxi6 libxrender1 libxtst6 \
  libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 \
  qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils \
  cpu-checker

# 4. Verificar que el hardware virtual (KVM) és disponible (per a l'emulador)
kvm-ok
# Si retorna "KVM acceleration can be used", l'emulador anirà fluid.
# Si no, compilar i testar en un dispositiu físic via USB.

# 5. Afegir l'usuari al grup kvm (tancar sessió i tornar a entrar després)
sudo usermod -aG kvm $USER
```

### Android Studio

```bash
# Descarregar Android Studio (Ladybug 2024.x o superior)
wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2024.2.1.12/android-studio-2024.2.1.12-linux.tar.gz \
  -O ~/Downloads/android-studio.tar.gz

# Extreure a /opt
sudo tar -xzf ~/Downloads/android-studio.tar.gz -C /opt/
sudo ln -sf /opt/android-studio/bin/studio.sh /usr/local/bin/android-studio

# Llançar
android-studio
```

Alternativament, instal·lar via **snap** (més senzill, però versió pot anar uns dies enrere):
```bash
sudo snap install android-studio --classic
```

Durant la primera execució d'Android Studio, completar el wizard:
- Instal·lar Android SDK (API 35 + API 26 com a mínim).
- Instal·lar Android Build-Tools 35.
- Instal·lar emulador (opcional, si KVM disponible).

### Variables d'entorn — afegir a `~/.bashrc` o `~/.profile`

```bash
# Android SDK (el path per defecte d'Android Studio a Linux)
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"

# Java 17
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$PATH:$JAVA_HOME/bin"
```

Aplicar canvis:
```bash
source ~/.bashrc
```

Verificar:
```bash
echo $ANDROID_HOME     # /home/usuari/Android/Sdk
adb --version          # Android Debug Bridge version ...
java -version          # openjdk 17
```

### Windsurf / Cursor en Ubuntu 24.04

**Windsurf:**
```bash
# Descarregar el .deb des de https://codeium.com/windsurf/download
wget https://windsurf-stable.codeiumdata.com/wVxQEIWkwPUEAGf3/apt/pool/main/w/windsurf/windsurf_<versio>_amd64.deb \
  -O ~/Downloads/windsurf.deb
sudo dpkg -i ~/Downloads/windsurf.deb
sudo apt --fix-broken install -y
```

O afegir el repositori oficial:
```bash
curl -fsSL "https://windsurf-stable.codeiumdata.com/wVxQEIWkwPUEAGf3/apt/stable.gpg" \
  | sudo gpg --dearmor -o /usr/share/keyrings/windsurf-stable.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/windsurf-stable.gpg] \
  https://windsurf-stable.codeiumdata.com/wVxQEIWkwPUEAGf3/apt stable main" \
  | sudo tee /etc/apt/sources.list.d/windsurf.list
sudo apt update && sudo apt install -y windsurf
```

**Cursor:**
```bash
# Descarregar AppImage des de https://www.cursor.com
wget https://downloader.cursor.sh/linux/appImage/x64 -O ~/Applications/cursor.AppImage
chmod +x ~/Applications/cursor.AppImage
~/Applications/cursor.AppImage
```

### GitHub — configuració inicial

```bash
git config --global user.name "El teu nom"
git config --global user.email "el-teu@email.com"

# Generar clau SSH per a GitHub (recomanat)
ssh-keygen -t ed25519 -C "el-teu@email.com"
cat ~/.ssh/id_ed25519.pub
# Copiar i afegir a GitHub → Settings → SSH and GPG keys
```

### Compilar l'app des de terminal (sense Android Studio)

Des del directori arrel del projecte:

```bash
# Donar permisos al wrapper de Gradle
chmod +x ./gradlew

# Compilar APK de debug
./gradlew assembleDebug

# APK resultant a:
ls app/build/outputs/apk/debug/app-debug.apk

# Instal·lar en dispositiu connectat per USB (amb depuració USB activada)
adb install app/build/outputs/apk/debug/app-debug.apk

# Compilar APK de release (sense signar)
./gradlew assembleRelease

# Netejar build anterior
./gradlew clean

# Executar tests unitaris
./gradlew test

# Veure tots els tasques disponibles
./gradlew tasks
```

### `local.properties` — configuració del SDK (generada automàticament per Android Studio)

Si es compila des de terminal sense Android Studio, crear manualment:

```
# fitxer: local.properties (a l'arrel del projecte, NO pujar a Git)
sdk.dir=/home/EL_TEU_USUARI/Android/Sdk
```

Substituir `EL_TEU_USUARI` pel nom d'usuari real del sistema Ubuntu.

### Dispositiu físic — connexió USB en Ubuntu 24.04

Si no es disposa d'emulador, connectar el mòbil Android per USB:

```bash
# Instal·lar regles udev per a dispositius Android
sudo apt install -y android-sdk-platform-tools-common

# O manualment, afegir regla udev per al fabricant del dispositiu:
# (exemple per a Samsung — substituir 04e8 pel Vendor ID del teu dispositiu)
echo 'SUBSYSTEM=="usb", ATTR{idVendor}=="04e8", MODE="0666", GROUP="plugdev"' \
  | sudo tee /etc/udev/rules.d/51-android.rules
sudo udevadm control --reload-rules
sudo udevadm trigger

# Verificar connexió
adb devices
# Ha de mostrar el dispositiu (no "unauthorized")
```

---

## ESTRUCTURA DEL PROJECTE

```
HoresFreelance/
├── .github/
│   └── workflows/
│       └── android.yml          ← CI + build APK release
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/freelance/hores/
│   │   │   │   ├── HoresApp.kt              ← Application class (Hilt)
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── DiaDao.kt
│   │   │   │   │   │   │   ├── ConcepteDao.kt
│   │   │   │   │   │   │   └── RangHorariDao.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── DiaEntity.kt
│   │   │   │   │   │       ├── ConcepteEntity.kt
│   │   │   │   │   │       └── RangHorariEntity.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── RegistreRepository.kt
│   │   │   │   │   └── export/
│   │   │   │   │       ├── CsvExporter.kt
│   │   │   │   │       └── PdfExporter.kt
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/
│   │   │   │   │       ├── Dia.kt
│   │   │   │   │       ├── Concepte.kt
│   │   │   │   │       └── RangHorari.kt
│   │   │   │   └── ui/
│   │   │   │       ├── navigation/
│   │   │   │       │   └── AppNavHost.kt
│   │   │   │       ├── theme/
│   │   │   │       │   ├── Color.kt
│   │   │   │       │   ├── Theme.kt
│   │   │   │       │   └── Type.kt
│   │   │   │       ├── screen/
│   │   │   │       │   ├── calendari/
│   │   │   │       │   │   ├── CalendariScreen.kt
│   │   │   │       │   │   └── CalendariViewModel.kt
│   │   │   │       │   ├── dia/
│   │   │   │       │   │   ├── DiaDetallScreen.kt
│   │   │   │       │   │   └── DiaDetallViewModel.kt
│   │   │   │       │   ├── registre/
│   │   │   │       │   │   ├── RegistreScreen.kt
│   │   │   │       │   │   └── RegistreViewModel.kt
│   │   │   │       │   └── resum/
│   │   │   │       │       ├── ResumScreen.kt
│   │   │   │       │       └── ResumViewModel.kt
│   │   │   │       └── component/
│   │   │   │           ├── RangHorariCard.kt
│   │   │   │           ├── ConcepteCard.kt
│   │   │   │           └── DiaCard.kt
│   │   │   ├── AndroidManifest.xml
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   └── colors.xml
│   │   │       └── mipmap-*/       ← icones de l'app
│   │   └── test/ + androidTest/    ← tests unitaris i d'integració bàsics
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
└── README.md
```

---

## MODEL DE DADES (Room)

### Entitat `DiaEntity`
```kotlin
@Entity(tableName = "dies")
data class DiaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: LocalDate,          // java.time.LocalDate
    val notes: String = ""        // notes opcionals del dia
)
```

### Entitat `ConcepteEntity`
```kotlin
@Entity(
    tableName = "conceptes",
    foreignKeys = [ForeignKey(
        entity = DiaEntity::class,
        parentColumns = ["id"],
        childColumns = ["diaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("diaId")]
)
data class ConcepteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diaId: Long,
    val nom: String               // p.ex: "Reunió client X", "Disseny web"
)
```

### Entitat `RangHorariEntity`
```kotlin
@Entity(
    tableName = "rangs_horaris",
    foreignKeys = [ForeignKey(
        entity = ConcepteEntity::class,
        parentColumns = ["id"],
        childColumns = ["concepteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("concepteId")]
)
data class RangHorariEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concepteId: Long,
    val horaInici: LocalTime,     // java.time.LocalTime
    val horaFi: LocalTime
)
```

**Converters Room necessaris:** implementar `TypeConverter` per a `LocalDate` i `LocalTime`
(serialitzar com a `Long` amb epoch day / second of day).

---

## PANTALLES I FUNCIONALITATS

### 1. Pantalla principal — Calendari (`CalendariScreen`)

- Vista de calendari mensual (implementar amb `HorizontalCalendar` propi o biblioteca
  `kizitonwose/calendar-compose`).
- Cada dia que té registres mostra un **punt o indicador** de color.
- Tap en un dia amb registres → navega a `DiaDetallScreen`.
- Tap en un dia buit → navega directament a `RegistreScreen` amb la data preseleccionada.
- Botó flotant (+) → `RegistreScreen` amb data d'avui per defecte.
- Barra superior amb accés a `ResumScreen` i a exportació.

### 2. Pantalla de detall del dia (`DiaDetallScreen`)

- Mostra la data del dia (format llarg: "Dilluns, 10 de juny de 2025").
- Mostra la nota del dia (editable in-line).
- Llista de **conceptes** del dia, cadascun amb:
  - Nom del concepte.
  - Llista dels rangs horaris (p.ex: "09:00 → 13:30 · 2h 30min").
  - Total d'hores del concepte.
  - Botó d'editar i esborrar.
- Total acumulat d'hores del dia.
- Botó per afegir un nou concepte al dia.
- Botó per editar o esborrar el dia sencer.

### 3. Pantalla de nou registre / edició (`RegistreScreen`)

Formulari en un sol scroll. Camps:

**Secció Data:**
- Selector de data (DatePickerDialog de Material3), per defecte avui.
- Camp de text per a notes del dia (opcional).

**Secció Conceptes:**
- Un o més **blocs de concepte**, cadascun amb:
  - Camp de text: nom del concepte (obligatori, ex: "Reunió client", "Programació").
  - Un o més **rangs horaris** per a aquell concepte:
    - Selector d'hora d'inici (TimePickerDialog Material3).
    - Selector d'hora de fi.
    - Càlcul automàtic de la durada i visualització (p.ex: "3h 15min").
    - Validació: hora fi > hora inici, avís si es solapen rangs del mateix concepte.
    - Botó per afegir un altre rang horari al mateix concepte.
    - Botó per esborrar el rang.
  - Botó per afegir un altre concepte al mateix dia.
  - Botó per esborrar el bloc de concepte.

**Botons:**
- "Desa" → guarda tots els conceptes i rangs; si el dia ja existeix, s'actualitza.
- "Cancel·la" → descarta canvis.

### 4. Pantalla de resum (`ResumScreen`)

- Selector de període: Aquesta setmana / Aquest mes / Mes anterior / Rang personalitzat.
- Per a cada dia del període seleccionat (si té registres):
  - Data + total hores del dia.
  - Llista de conceptes amb hores de cada concepte.
- Total d'hores del període seleccionat.
- Taula resum per concepte: nom → total hores en el període.
- Botó "Exportar CSV" → genera i comparteix un fitxer `.csv`.
- Botó "Exportar PDF" → genera i comparteix un fitxer `.pdf`.

---

## EXPORTACIÓ

### Format CSV
```
Data,Concepte,Hora inici,Hora fi,Durada (h),Notes dia
2025-06-10,Reunió client X,09:00,11:30,2.50,
2025-06-10,Programació web,13:00,17:00,4.00,Revisió final
2025-06-11,Disseny,10:00,12:00,2.00,
...
TOTAL,,,,8.50,
```

Capçaleres en el idioma de l'app. Compartit via `FileProvider` + `Intent.ACTION_SEND`.

### Format PDF
Generar amb la biblioteca `itextpdf` (iText 7 community) o `apache pdfbox` per Android.

Contingut del PDF:
- Títol: "Resum d'hores — [període]"
- Taula amb columnes: Data | Concepte | Inici | Fi | Durada
- Subtotals per dia
- Total general al peu
- Format A4, font llegible, estil minimalista

---

## NAVEGACIÓ

```
CalendariScreen (ruta: "calendari")   ← pantalla inicial
    ↓ tap dia amb registres
DiaDetallScreen (ruta: "dia/{diaId}")
    ↓ tap editar
RegistreScreen (ruta: "registre?diaId={diaId}&data={data}")
    ↑ també accessible des de CalendariScreen (nou registre)

ResumScreen (ruta: "resum")           ← des del menú superior
```

---

## UX / UI

- **Material Design 3** complet: colors dinàmics (Dynamic Color) si el dispositiu ho suporta.
- Color principal suggerit: blau fosc o verd petrol (professional).
- **Mode fosc** suportat automàticament.
- Totes les llistes amb `LazyColumn`.
- Loading states amb `CircularProgressIndicator`.
- Empty states amb il·lustració/text quan no hi ha registres.
- Confirmació (AlertDialog) abans d'esborrar qualsevol registre.
- Snackbar de confirmació en desar o esborrar.
- L'app ha de ser **completament funcional en català, castellà i anglès** (strings.xml).
  Idioma automàtic segons el dispositiu.

---

## GITHUB I CI/CD

### Repositori GitHub
1. Crear un repositori públic o privat: `hores-freelance-android`.
2. Afegir `.gitignore` per a Android (incloure `/build`, `*.keystore`, `/local.properties`).
3. `README.md` amb:
   - Descripció de l'app.
   - Screenshots (placeholders inicialment).
   - Instruccions per compilar.
   - Llicència MIT.

### GitHub Actions (`android.yml`)

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run unit tests
        run: ./gradlew test

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk

  release:
    runs-on: ubuntu-latest
    needs: build
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission
        run: chmod +x gradlew

      - name: Build release APK (unsigned)
        run: ./gradlew assembleRelease

      - name: Upload release APK
        uses: actions/upload-artifact@v4
        with:
          name: app-release-unsigned
          path: app/build/outputs/apk/release/app-release-unsigned.apk
```

> Per a una APK signada en producció, configurar un keystore i secrets de GitHub
> (`KEYSTORE_FILE`, `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD`) i afegir el pas
> de signatura amb `gradle-sign` o `zipalign + apksigner`.

---

## TESTS

- **Tests unitaris** (JVM): testar els `ViewModel` amb `kotlinx-coroutines-test` i
  `turbine` per a StateFlow.
- **Tests d'integració** (Room in-memory): verificar que les operacions CRUD funcionen.
- Cobertura mínima: operacions de creació, edició i esborrat de dies, conceptes i rangs.

---

## DEPENDÈNCIES GRADLE (app/build.gradle.kts)

```kotlin
dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity + ViewModel + Navigation
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Calendari (opcional, recomanat)
    implementation("com.kizitonwose.calendar:compose:2.5.0")

    // PDF (triar una opció)
    // Opció A - iText7 community (llicència AGPL):
    // implementation("com.itextpdf:itext7-core:7.2.5")
    // Opció B - PdfDocument Android natiu (sense dependència externa):
    // Usar android.graphics.pdf.PdfDocument (disponible des de API 19)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("io.mockk:mockk:1.13.12")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## INSTRUCCIONS ESPECÍFIQUES PER A LA IA

1. **Comença** creant l'estructura de fitxers i el `build.gradle.kts` complet.
2. **Implementa el model de dades** (entities, DAOs, converters, AppDatabase).
3. **Implementa el Repository** amb totes les operacions CRUD i les queries necessàries.
4. **Implementa els ViewModels** un per un, amb StateFlow.
5. **Implementa les pantalles** en ordre: CalendariScreen → DiaDetallScreen → RegistreScreen → ResumScreen.
6. **Implementa els components** reutilitzables.
7. **Implementa l'exportació** CSV i PDF.
8. **Configura la navegació** completa.
9. **Crea el workflow de GitHub Actions**.
10. **Escriu els tests** bàsics.
11. **Genera el README.md**.

En cada pas, verifica que el codi compila i no té errors abans de continuar.
Si cal escollir entre dues solucions, escull la més simple i mantenible.
Comenta el codi en anglès (comentaris inline breus).
No afegeixis funcionalitats que no estiguin especificades aquí.

---

## RESULTAT ESPERAT

Al final del procés, el repositori GitHub ha de contenir:
- Codi font complet i funcional.
- L'app ha de compilar sense errors amb `./gradlew assembleDebug`.
- El workflow de CI ha de passar correctament.
- La carpeta `app/build/outputs/apk/` ha de contenir l'APK.
- El `README.md` ha d'explicar com compilar i instal·lar l'app.
