# Documentació Final del Projecte: HoresFreelance

## Resum del Projecte
HoresFreelance és una aplicació Android nativa per a autònoms per registrar hores de treball, organitzar-les per "Bolos" (projectes/clients), i generar informes (CSV/PDF) per a la facturació.

## Stack Tecnològic
- **Llenguatge:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Arquitectura:** MVVM + Repository Pattern
- **Persistència:** Room Database (SQLite)
- **Injecció de Dependències:** Hilt
- **Asincronia:** Kotlin Coroutines & Flow
- **Navegació:** Navigation Compose

## Evolució i Correccions Realitzades (Històric)
1. **Configuració Inicial i CI/CD:** S'han afegit fitxers crítics (`AndroidManifest.xml`, `build.gradle.kts`, `file_paths.xml`, etc.) per garantir la compilació i la funcionalitat de `FileProvider`.
2. **Correcció de Bugs Crítics:**
    - **Persistència:** Refactorització del `saveDia()` al Repository per evitar duplicats en editar (esborrat previ de relacions).
    - **Exportació:** Modificació d'`ExportService` per retornar `Intent` i ser llançat des de l'Activity, evitant crashes de seguretat en Android 10+.
    - **Race Conditions:** Implementació de `flatMapLatest` al `CalendariViewModel` i `ResumViewModel` per gestionar correctament les subscripcions asíncrones.
    - **Integritat BD:** Activació explícita de `PRAGMA foreign_keys=ON;` al callback de Room.
3. **Millores Beta:**
    - **UI/UX:** Marcador visual per al dia actual al calendari i suport per preu/hora variable per bolo.
    - **Terminologia:** Adaptació de tota la interfície gràfica de "Concepte" a "Bolo".
    - **Localització:** Implementació robusta de traductors (`strings.xml`) per a Català, Castellà i Anglès, amb formatatge de dates `Locale.getDefault()` i separadors decimals `Locale.US` per a CSVs.
4. **Optimitzacions:**
    - Eliminació de permisos innecessaris (`WRITE/READ_EXTERNAL_STORAGE`).
    - Neteja de dependències no utilitzades (calendari extern eliminat).
    - Correcció del format dels `strings.xml` per a múltiples paràmetres (`%1$s`, `%2$s`).

## CI/CD (GitHub Actions)
La pipeline a `.github/workflows/android.yml` està configurada per:
- Compilar l'app (`assembleDebug`).
- Executar tests unitaris (`test`).
- Executar tests instrumentats en emulador (API 31, arquitectura `x86_64` optimitzada).
- Generar artefactes d'instal·lació.
