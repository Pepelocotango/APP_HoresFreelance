# Informe d'Anàlisi del Projecte: HoresFreelance DESACTUALITZAT!!

## 1. Introducció
HoresFreelance és una aplicació Android nativa dissenyada per a treballadors autònoms. Permet portar un control exhaustiu de les hores de treball, organitzades per "Bolos" (projectes o tasques), gestionar clients, preus per hora, despeses associades i generar informes per a la facturació.

## 2. Arquitectura del Sistema
L'aplicació segueix els principis de **Clean Architecture** i el patró **MVVM (Model-View-ViewModel)** de manera estricta:

- **Capa de Dades (Data):** Utilitza **Room** per a la persistència local. Està organitzada en entitats (`DiaEntity`, `ConcepteEntity`, `RangHorariEntity`, `ClientEntity`) i DAOs. Inclou una migració robusta (v3 a v4) per suportar estats de facturació i despeses.
- **Capa de Domini (Domain):** Defineix els models de negoci concrets (`Dia`, `Concepte`, `RangHorari`, `Client`) que són independents de la base de dades.
- **Capa de Presentació (UI):** Construïda íntegrament amb **Jetpack Compose** i **Material 3**. La lògica d'estat es gestiona amb `ViewModels` i `StateFlow`.
- **Injecció de Dependències:** S'utilitza **Hilt** per gestionar el cicle de vida i la injecció de components (Base de dades, Repositoris, Serveis).

## 3. Funcionalitats Principals
- **Registre de Jornada:** Permet afegir múltiples rangs horaris a un mateix "Bolo". Suporta el càlcul automàtic d'hores (incloent-hi els canvis de dia a mitjanit).
- **Fitxatge Ràpid:** Inclou una funcionalitat de "Start/Stop" que arrodoneix l'hora d'inici i fi als 15 minuts més propers.
- **Gestió de Clients:** Pantalla dedicada per definir clients amb preus per hora per defecte.
- **Visualització en Calendari:** Vista mensual amb indicadors de dies treballats.
- **Resum i Gràfics:** Pantalla de resum amb filtres per estat (Pendent, Facturat, Cobrat) i per client, incloent un gràfic de barres de guanys.
- **Exportació i Còpies de Seguretat:**
    - Generació de fitxers **CSV** i **PDF** detallats.
    - Sistema d'importació/exportació de la base de dades local (.db).

## 4. Estat del Codi i Qualitat
- **Compilació:** El projecte compila correctament amb Gradle 8.7 i JDK 21.
- **Tests:** Disposa de tests unitaris per als models de domini (`ModelTests.kt`) que verifiquen els càlculs d'hores i imports. Tots els tests passen satisfactòriament.
- **Idioma:** Segueix la convenció d'utilitzar el català per a comentaris, missatges de la UI i documentació interna.

## 5. Observacions i Possibles Millores
Durant l'anàlisi s'han detectat alguns punts d'optimització:
1. **APIs Deprecated:** Alguns components de Compose (com `menuAnchor`) utilitzen versions antigues que s'haurien d'actualitzar a les noves sobrecàrregues de Material 3.
2. **Neteja de Codi:** Hi ha alguns paràmetres no utilitzats en components de la UI (`Cards.kt`, `FormValidator.kt`) que generen warnings de compilació.
3. **Iconografia:** S'utilitza `Icons.Filled.List`, el qual està marcat per ser reemplaçat per la seva versió *AutoMirrored*.

## 6. Conclusió
HoresFreelance és un projecte madur, ben estructurat i que segueix les millors pràctiques actuals de desenvolupament Android. És fàcilment escalable i manté una separació clara de responsabilitats.
