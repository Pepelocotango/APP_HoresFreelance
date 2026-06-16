# HoresFreelance

A native Android app for freelancers to track their work hours, manage projects ("Bolos"), and generate invoices.

---

## 🇬🇧 English

HoresFreelance is a native Android application designed to help freelancers track their work hours, organize them by "Bolos" (projects/clients) with individual hourly rates, and generate invoices in CSV or PDF formats.

### Features
- 📅 **Calendar View**: Visual calendar showing work days.
- ⏱️ **Time Tracking**: Record work hours by "Bolo" (concept) and define an individual price per hour.
- 📊 **Hours Summary**: View work hours and earnings by week, month, or custom range.
- 💾 **Local Storage**: All data stored offline on device using Room.
- 📥 **Export**: Export reports as CSV and PDF.

### Project Structure
```text
HoresFreelance/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/freelance/hores/
│   │   │   │   ├── HoresApp.kt              # Application class
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/                 # Room database & Entities
│   │   │   │   │   ├── repository/         # Data layer
│   │   │   │   │   └── export/             # CSV/PDF exporters
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/              # Domain models
│   │   │   │   └── ui/
│   │   │   │       ├── screen/             # UI screens
│   │   │   │       ├── component/          # Reusable components
│   │   │   │       └── theme/              # Material 3 theme
│   │   │   └── res/                        # Resources (XML, values, mipmap)
│   │   ├── test/                           # Unit tests
│   │   └── androidTest/                    # Instrumented tests
│   └── build.gradle.kts
├── .github/workflows/
│   └── android.yml                         # CI/CD workflow
├── build.gradle.kts
└── settings.gradle.kts
```

---

## [CAT] Català

HoresFreelance és una aplicació Android nativa dissenyada per ajudar els treballadors autònoms a registrar les seves hores de treball, organitzar-les per "Bolos" (projectes/clients) amb preus per hora individuals, i generar informes en format CSV o PDF per a la facturació.

### Funcionalitats
- 📅 **Vista Calendari**: Calendari visual per veure els dies de treball.
- ⏱️ **Registre d'Hores**: Registra hores de treball per "Bolo" (concepte) i defineix un preu per hora individual.
- 📊 **Resum d'Hores**: Consulta les hores treballades i els guanys per setmana, mes o període personalitzat.
- 💾 **Emmagatzematge Local**: Totes les dades s'emmagatzemen offline al dispositiu mitjançant Room.
- 📥 **Exportació**: Exporta informes en format CSV i PDF.

### Estructura del Projecte
(Vegeu l'arbre de fitxers a la secció en anglès superior)

---

## ⚙️ CI/CD (GitHub Actions)
The project includes an automated pipeline in `.github/workflows/android.yml` that handles:
- Compiling the app (`assembleDebug`).
- Running unit tests (`test`).
- Running instrumented tests in an emulator (API 31, `x86_64`).
- Generating installable artifacts (APKs).

El projecte inclou una pipeline automatitzada a `.github/workflows/android.yml` que s'encarrega de:
- Compilar l'app (`assembleDebug`).
- Executar tests unitaris (`test`).
- Executar tests instrumentats en emulador (API 31, `x86_64`).
- Generar artefactes d'instal·lació (APKs).

## 🚀 Building & Running

### Requirements
- Android 8.0+ (API level 26)
- JDK 17
- Android SDK 35
- Gradle 8.0+

### Build
```bash
./gradlew assembleDebug
```

## License
MIT License
