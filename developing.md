# Guia de Desenvolupament: HoresFreelance

## Estat Actual
L'aplicació ha assolit la plena compatibilitat multiplataforma:
- **Estructura de Dades Unificada**: Totes les plataformes utilitzen identificadors tipus `String` (UUID) per garantir la compatibilitat del JSON.
- **Android (Room)**: Utilitza Room per a l'emmagatzematge local.
- **Compatibilitat JSON**: Servei d'exportació/importació JSON per sincronitzar dades entre dispositius.
- **Internacionalització (i18n)**: Suport per a `ca`, `es` i `en`.

## Arquitectura de Dades
Per mantenir la compatibilitat entre Android i la PWA, cal seguir aquestes regles:
1. **Identificadors**: Utilitzar sempre UUIDs generats al client.
2. **Dates**: Format `YYYY-MM-DD` (ISO).
3. **Hores**: Format `HH:mm`.
4. **Estats**: Utilitzar valors coherents (`PENDENT`, `FACTURAT`, `COBRAT` o equivalents en codi).
5. **Noms de Camps**: Mantenir els noms seguint l'estàndard definit a `AppData.kt` (Android) i `types.ts` (PWA).

## Com treballar amb els models
- **Android**: Els models de domini a `domain/model` contenen la lògica de negoci (càlcul de totals).
- **PWA**: Els models estan definits a `src/types.ts` i la lògica a `src/lib/utils.ts`.

## Internacionalització (i18n)
Tots els textos de la interfície PWA es gestionen a `app_PWA/horesfreelance/src/locales/`.
- Per afegir un nou idioma: crear un nou fitxer JSON a la carpeta `locales/` i registrar-lo a `i18n.ts`.
- Mantenir les claus de traducció sincronitzades en tots els fitxers JSON.

## Normes de codi
- **Idioma**: Tota la interfície, comentaris i documentació interna han d'estar en **català** (excepte les traduccions als fitxers JSON de `locales/` que suporten altres idiomes).
- **Tests**: Cada vegada que es modifica la lògica de càlcul als models de domini, cal actualitzar `ModelTests.kt`.
