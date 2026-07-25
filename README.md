<div align="center">

# 💊 PillsOClock

**Non dimenticare mai più una dose.**

App Android nativa per ricordare le assunzioni di farmaci durante la giornata,
con promemoria puntuali anche ad app chiusa.

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room-2.8.4-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-27%20(Android%208.1)-orange)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)

</div>

---

## 📖 Overview

**PillsOClock** è un'app Android **monoutente** (nessun login, un solo profilo
per installazione) pensata per chi deve gestire più farmaci con orari e
frequenze diverse. In home l'utente vede a colpo d'occhio le assunzioni
previste per oggi, può segnarle come assunte con un tap e riceve una
notifica puntuale esatta all'orario giusto — anche se il telefono è in Doze
o l'app è stata chiusa. Ogni dose presa resta consultabile nello storico,
anche se il farmaco viene in seguito modificato, messo in pausa o eliminato.

## ✨ Funzionalità principali

- 🏠 **Home intelligente** — prossima assunzione, dosi in ritardo e dosi
  imminenti raggruppate separatamente, con barra di aderenza giornaliera.
- 🔁 **Piani di assunzione flessibili** — giornaliera, ogni N giorni, o su
  giorni specifici della settimana, con uno o più orari per farmaco.
- 📅 **Frequenza & storico** — calendario con i giorni in cui sono state
  registrate assunzioni, drill-down sul dettaglio di ogni giornata.
- 🔔 **Promemoria esatti** — notifica push puntuale via `AlarmManager`
  (`setExactAndAllowWhileIdle`), sopravvive a riavvii e terminazione del processo.
- 🌙 **Dark mode** — preferenza persistita con Preferences DataStore.
- 🗑️ **Soft delete ovunque** — disattivare/eliminare un farmaco o un orario
  non cancella mai lo storico delle dosi già assunte.
- ⏱️ **Finestra di conferma** — possibilità di segnare una dose come assunta
  in anticipo, puntuale (±5 min) o in ritardo, con conferme animate.

## 📸 Screenshot

> _In arrivo — sezione da completare con le immagini dell'app._

<!--
| Home | Medicinali | Frequenza |
|:---:|:---:|:---:|
| ![Home](docs/screenshots/home.png) | ![Medicinali](docs/screenshots/medicinali.png) | ![Frequenza](docs/screenshots/frequenza.png) |

| Aggiungi medicinale | Impostazioni |
|:---:|:---:|
| ![Aggiungi](docs/screenshots/aggiungi.png) | ![Impostazioni](docs/screenshots/impostazioni.png) |
-->

## 🏗️ Architettura

Architettura a livelli con separazione netta tra dominio e dettagli di
persistenza/UI, MVVM lato presentazione e **dependency injection manuale**
(nessun framework come Hilt):

```
domain/
├── model/          → data class pure, zero dipendenze Android/Room
└── repository/     → interfacce dei repository (contratti)

data/
├── local/
│   ├── entity/     → entity Room (@Entity)
│   └── dao/        → interfacce @Dao
├── mapper/         → EntityX.toDomain() / DomainX.toEntity()
└── repository/     → implementazioni concrete (*RepositoryImpl)

ui/
└── <schermata>/    → Composable + ViewModel + ViewModelFactory
```

I repository vengono esposti come proprietà `lazy` da `PillsOClockApp`
(`Application`), e iniettati manualmente nelle `ViewModelFactory` di ogni
schermata. La generazione giornaliera delle assunzioni è affidata a
**WorkManager** (worker periodico + one-shot al primo avvio), mentre i
promemoria puntuali passano da **AlarmManager**: due meccanismi
complementari, non alternativi.

## 🛠️ Stack tecnologico

| Categoria | Libreria | Versione |
|---|---|---|
| Linguaggio | Kotlin | 2.3.21 |
| UI | Jetpack Compose (BOM) + Material 3 | 2026.06.01 |
| Navigazione | Navigation 3 (`androidx.navigation3`) | 1.1.4 |
| Persistenza | Room | 2.8.4 |
| Background/scheduling | WorkManager | 2.11.2 |
| Notifiche puntuali | AlarmManager (Android SDK) | — |
| Preferenze | DataStore Preferences | 1.1.1 |
| Serializzazione route | Kotlinx Serialization | 1.11.0 |
| Analytics | Firebase Analytics (BOM) | 34.16.0 |
| Codegen | KSP | 2.3.9 |
| UI di supporto | ConstraintLayout Compose | 1.1.1 |
| Splash screen | AndroidX Core SplashScreen | 1.2.0 |

## 📋 Requisiti

- **minSdk** 27 (Android 8.1 Oreo)
- **compileSdk / targetSdk** 37 / 36
- **AGP** 9.2.0
- Android Studio con supporto Kotlin 2.3.x

## 🚀 Setup & build

Comandi Gradle standard, da eseguire dalla root del repo:

```bash
# Build & install
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build e installa su device/emulatore collegato

# Unit test (JVM locale)
./gradlew test

# Instrumented test (richiede device/emulatore)
./gradlew connectedAndroidTest

# Lint Android
./gradlew lint
```

<div align="center">

Fatto con 🧡 in Kotlin & Jetpack Compose

</div>
