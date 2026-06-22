# HoresFreelance

Aplicació multiplataforma (Android Nativa + PWA/Electron) dissenyada per ajudar els treballadors autònoms a registrar les seves hores de treball, organitzar-les per "Bolos" (projectes/clients) i gestionar la facturació.

## 🇬🇧 English

HoresFreelance is a dual-platform application (Native Android + React PWA/Electron) designed to help freelancers track their work hours, organize them by "Bolos" (projects/clients) with individual hourly rates, and generate invoices.

### Key Features
- 📅 **Calendar View**: Visual calendar to manage work days.
- ⏱️ **Time Tracking**: Record work hours by "Bolo", individual rates, billing status (Pending/Invoiced/Collected), and expenses.
- 📊 **Summary & Reports**: View earnings, hours, and expenses with visual charts.
- 👥 **Client Management**: Define clients with custom settings.
- 🔄 **Cross-Platform Compatibility**: Data is compatible between Android and PWA/Electron via a unified JSON format.
- 🌍 **Internationalization (i18n)**: Available in Catalan (CA), Spanish (ES), and English (EN).
- 💾 **Local Storage**: Data stored offline for total privacy.
- 📥 **Export**: Export reports as CSV and PDF.

---

## [CAT] Català

HoresFreelance és una aplicació multiplataforma (Android Nativa + PWA React/Electron) dissenyada per ajudar els treballadors autònoms a registrar les seves hores de treball, organitzar-les per "Bolos" (projectes/tasques) i generar informes de facturació.

### Funcionalitats Principals
- 📅 **Vista Calendari**: Calendari visual per gestionar els dies de treball.
- ⏱️ **Registre d'Hores**: Registra hores per "Bolo" amb preu hora, estat de facturació i despeses.
- 📊 **Resum i Gràfics**: Consulta guanys i hores per períodes amb desglossament detallat.
- 👥 **Gestió de Clients**: Defineix clients amb tarifes personalitzades.
- 🔄 **Compatibilitat Total**: Les dades són compatibles entre Android i PWA/Electron mitjançant un format JSON unificat.
- 🌍 **Internacionalització**: L'aplicació suporta Català (CA), Castellà (ES) i Anglès (EN).
- 💾 **Emmagatzematge Local**: Les dades es guarden al dispositiu per a total privadesa.

---

## ⚙️ CI/CD (GitHub Actions)
El projecte inclou pipelines automatitzades:
- **Android**: Compila APKs i executa tests.
- **PWA/Electron**: Instala dependències, genera el bundle de producció i artefactes.

## 🚀 Building

### Android
```bash
./gradlew assembleDebug
```

### PWA/Electron
```bash
cd app_PWA/horesfreelance
npm install
npm run build
```

## Llicència
MIT License
