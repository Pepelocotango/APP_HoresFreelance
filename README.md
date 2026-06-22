# HoresFreelance DESACTUALITZAT!!

A native Android app and PWA/Desktop app for freelancers to track their work hours, manage projects ("Bolos"), and generate invoices.

---

## 🇬🇧 English

HoresFreelance is a dual-platform application (Native Android + React PWA) designed to help freelancers track their work hours, organize them by "Bolos" (projects/clients) with individual hourly rates, and generate invoices.

### Features
- 📅 **Calendar View**: Visual calendar showing work days on both platforms.
- ⏱️ **Time Tracking**: Record work hours by "Bolo" (concept), individual rates, **billing status (Pending/Invoiced/Paid)**, and **expenses**.
- 📊 **Hours Summary**: View work hours, earnings, and **expenses** with visual charts.
- 👥 **Client Management**: Manage clients and their default rates.
- 💾 **Local Storage**: Data stored offline (Room on Android, LocalStorage on PWA).
- 🔄 **Cross-Platform Compatibility**: Full data compatibility via **JSON export/import**. Start tracking on Android and continue on Desktop/PWA.
- 📥 **Export**: Export reports as CSV and PDF.

### Project Structure
```text
HoresFreelance/
├── app/                    # Native Android Application (Kotlin, Compose, Room)
├── app_PWA/                # Desktop/Web Application (React, Vite, Zustand)
├── .github/workflows/
│   ├── android.yml         # Android CI/CD
│   └── pwa.yml             # PWA CI/CD
└── ...
```

---

## [CAT] Català

HoresFreelance és una aplicació multiplataforma (Android Nativa + PWA React) dissenyada per ajudar els treballadors autònoms a registrar les seves hores de treball, organitzar-les per "Bolos" (projectes/tasques) i generar informes de facturació.

### Funcionalitats Principals
- 📅 **Vista Calendari**: Calendari visual per gestionar els dies de treball.
- ⏱️ **Registre d'Hores**: Registra hores per "Bolo" amb preu hora, estat de facturació i despeses.
- 📊 **Resum i Gràfics**: Consulta guanys i hores per períodes amb desglossament detallat.
- 👥 **Gestió de Clients**: Defineix clients amb tarifes personalitzades.
- 🔄 **Compatibilitat Total**: Les dades són compatibles entre Android i PWA mitjançant un format **JSON unificat**. Pots moure les teves dades d'un dispositiu a un altre fàcilment.
- 💾 **Emmagatzematge Local**: Les dades es guarden al dispositiu per a total privadesa.

---

## ⚙️ CI/CD (GitHub Actions)
The project includes automated pipelines for both platforms:
- **Android**: Compiles APKs (Debug/Release), runs unit and instrumented tests.
- **PWA**: Installs dependencies, lints code, builds the production bundle, and generates a ZIP artifact.

## 🚀 Building

### Android
```bash
./gradlew assembleDebug
```

### PWA
```bash
cd app_PWA/horesfreelance
npm install
npm run build
```

## License
MIT License
