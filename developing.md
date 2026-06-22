# Guia de Desenvolupament: HoresFreelance DESACTUALITZAT!!

## Estat Actual (v5)
L'aplicació ha assolit la plena compatibilitat multiplataforma:
- **Estructura de Dades Unificada**: Totes les plataformes utilitzen identificadors tipus `String` (UUID) per garantir la compatibilitat del JSON.
- **Android (Room v5)**: Migració destructiva realitzada per canviar les claus primàries de `Long` a `String`.
- **Compatibilitat JSON**: S'ha implementat un servei d'exportació i importació JSON basat en `kotlinx.serialization` (Android) i `JSON.parse` (PWA).
- **GitHub Actions Dual**: S'han separat els workflows per a Android i PWA, generant artefactes independents.

## Arquitectura de Dades
Per mantenir la compatibilitat entre Android i la PWA, cal seguir aquestes regles:
1. **Identificadors**: Utilitzar sempre UUIDs generats al client.
2. **Dates**: Format `YYYY-MM-DD` (ISO).
3. **Hores**: Format `HH:mm`.
4. **Estats**: Utilitzar valors en majúscules (`PENDENT`, `FACTURAT`, `COBRAT`).
5. **Noms de Camps**: Mantenir els noms en català seguint l'estàndard definit a `AppData.kt` (Android) i `types.ts` (PWA).

## Com treballar amb els models
- **Android**: Els models de domini a `domain/model` contenen la lògica de negoci (càlcul de totals). No s'ha de duplicar aquesta lògica a la UI.
- **PWA**: Els models estan definits a `src/types.ts` i la lògica a `src/lib/utils.ts`.

## CI/CD
- Els canvis a la carpeta `app/` activen el workflow d'Android.
- Els canvis a `app_PWA/` s'haurien de verificar manualment o mitjançant `pwa.yml`.
- Els artefactes de la PWA segueixen la nomenclatura: `app_PWA-C${run_number}.zip`.

## Normes de codi
- **Idioma**: Tota la interfície, comentaris i documentació interna han d'estar en **català**.
- **Tests**: Cada vegada que es modifica la lògica de càlcul als models de domini, cal actualitzar `ModelTests.kt`.
