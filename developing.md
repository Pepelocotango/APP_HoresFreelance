# Guia de Desenvolupament: HoresFreelance

## Estat Actual (v4)
L'aplicació ha evolucionat per incloure:
- **Persistència v4**: Migració de Room que suporta l'estat de facturació i despeses associades als conceptes.
- **UI Material 3**: Navegació inferior integrada i pantalles actualitzades (Resum, Clients, Registre).
- **Funcionalitat Backup**: Servei de còpia de seguretat de la base de dades local.

## Com afegir noves funcionalitats
1. **Models/BD**: Si calen noves dades, actualitzar l'entitat, crear la migració a `AppDatabase.kt` i actualitzar els convertidors.
2. **Repositori**: Assegurar la injecció de nous DAOs si és necessari.
3. **UI**: Seguir les convencions de Material 3 i `Navigation Compose` definides a `MainActivity` i `AppNavHost`.

## Normes de codi
- Idioma: Comentaris i missatges en català.
- Arquitectura: MVVM estricte.
- Tests: Afegir tests unitaris als nous viewmodels i repositoris abans de commitejar.
