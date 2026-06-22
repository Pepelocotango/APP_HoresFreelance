# Canvi necessari al build.gradle.kts ARREL

Al fitxer `./build.gradle.kts` (el de l'arrel del projecte, no el del shared),
eliminar la línia del Room Gradle Plugin:

```kotlin
// ELIMINAR aquesta línia:
id("androidx.room") version "2.7.0-alpha01" apply false
```

I actualitzar les versions de Kotlin i KSP si estan a alpha01:

```kotlin
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false      // 1.9.23 → 1.9.24
    id("org.jetbrains.kotlin.multiplatform") version "1.9.24" apply false // 1.9.23 → 1.9.24
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false   // 1.9.23-1.0.19 → 1.9.24-1.0.20
    // androidx.room NO va aquí
}
```

## Per què s'elimina el Room Gradle Plugin (RGP)?

El plugin `id("androidx.room")` va ser dissenyat per simplificar la configuració
de Room a projectes Android purs. En projectes KMP, interfereix amb el KSP
de dues maneres:

1. Intenta registrar el processador KSP per a tots els targets de forma
   automàtica, **sobreescrivint** o **entrant en conflicte** amb les
   entrades manuals de `kspCommonMainMetadata`, `kspAndroid`, etc.

2. El bloc `room { schemaDirectory(...) }` que activa el plugin configura
   el KSP amb arguments propis que entren en conflicte amb la detecció
   de tipus del `commonMain`, causant exactament l'error:
   `[MissingType]: Element 'AppDatabase' references a type that is not present`

La configuració manual amb `add("kspCommonMainMetadata", ...)` és
l'única manera correcta de configurar Room KMP.
