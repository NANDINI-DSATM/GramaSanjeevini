# Grama Sanjeevini

**Project Overview**
- **Description:** Grama Sanjeevini is an Android application that connects rural pharmacies into a searchable network so users and local pharmacists can find medicines, view emergency (life‑saving) drug availability, and manage store inventory.
- **Primary Use Cases:** Search medicines across nearby village shops, view life‑saving emergency medicines, and provide a simple pharmacist dashboard to manage stock and expiry alerts.

**Features**
- **Search Medicines:** Free‑text search across all shops for medicine names with price and availability shown.
- **Emergency Medicines:** Lists medicines flagged as life‑saving and available now.
- **Pharmacist Login & Dashboard:** Pharmacists can login with phone + 4‑digit PIN to manage their shop stock (add / edit / delete items) and view expiry alerts.
- **Expiry Alerts:** Automatic detection of items expiring within 30 days for each shop.
- **Mock Data Seeder:** Repository includes a helper to seed demo shops and stock items for development.

**Technical Stack**
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM using ViewModels and repository pattern
- **Backend / Data:** Firebase Firestore (Realtime snapshots via listeners)
- **Build:** Gradle (Kotlin DSL) with the Gradle wrapper
- **Minimum SDK:** 24
- **Target / Compile SDK:** 36
- **JVM / Toolchain:** Java 11 compatibility

**Key Libraries and Tools**
- AndroidX Compose (BOM) — Compose UI, Material3, Navigation, ViewModel integration
- Firebase BoM + Firestore KTX
- Kotlin coroutines and Flow for async data streams

**Repository Layout**
- **Module:** [app](app)
- **Entry:** [MainActivity.kt](app/src/main/java/com/dsatm/gramasanjeevini/MainActivity.kt#L1-L40)
- **Navigation graph:** [NavGraph.kt](app/src/main/java/com/dsatm/gramasanjeevini/ui/navigation/NavGraph.kt#L1-L120)
- **UI screens:** [ui/screens](app/src/main/java/com/dsatm/gramasanjeevini/ui/screens) — Home, Search, Emergency, Pharmacist login & dashboard
- **ViewModels:** [ui/viewmodel](app/src/main/java/com/dsatm/gramasanjeevini/ui/viewmodel) — state & business logic
- **Data / Repository:** [data/repository/FirestoreRepository.kt](app/src/main/java/com/dsatm/gramasanjeevini/data/repository/FirestoreRepository.kt#L1-L60)
- **Models:** [data/model](app/src/main/java/com/dsatm/gramasanjeevini/data/model)

**Important Files**
- Project build: [build.gradle.kts](build.gradle.kts)
- App module build: [app/build.gradle.kts](app/build.gradle.kts#L1-L120)
- Gradle settings: [settings.gradle.kts](settings.gradle.kts)
- Firebase config (if present): [app/google-services.json](app/google-services.json) (this repo includes a google‑services.json; ensure it matches your Firebase project before publishing)

Getting Started
---------------

Prerequisites
- Android Studio (recommended) or CLI with Android SDK
- JDK 11
- Connected device or emulator running Android 7.0 (API 24) or higher
- A Firebase project with Firestore enabled (optional for running with real data)

Local Setup (Android Studio)
1. Open Android Studio and choose "Open" → select the repository root folder.
2. Let Gradle and the IDE sync and download required plugins and dependencies.
3. If you plan to use a Firebase project, confirm `app/google-services.json` corresponds to your Firebase project. If you need to replace it, download the JSON from the Firebase Console and put it at `app/google-services.json`.
4. Run the app from Android Studio (Run → Run 'app') on a device or emulator.

CLI Build & Run
1. From repository root (Windows):

```powershell
./gradlew.bat assembleDebug
./gradlew.bat installDebug
```

2. Or on macOS / Linux:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Firebase configuration notes
- The app uses Firebase Firestore. Ensure the Firestore rules allow reads/writes for development or configure appropriate security rules.
- The provided `FirestoreRepository.seedMockData()` helper will clear existing `shops` and `stock` collections and seed demo shops + items. Use it only in development. There is no UI trigger by default — call it from a temporary test entry point or unit test while developing.

Seeding Demo Data (development only)
1. From a temporary debug coroutine (or unit test) call `FirestoreRepository().seedMockData()` to populate demo shops and stock items.
2. After seeding, open the app and use the Search / Emergency screens to see demo data.

Testing
- The project includes typical Android instrumented and local unit test dependencies (see `app/build.gradle.kts`). Run tests with:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

Development Notes
- Code style: Kotlin `official` code style is configured in `gradle.properties`.
- Compose: UI is fully implemented in Jetpack Compose with a single‑activity `NavGraph` navigation setup.
- Data flows: Firestore snapshot listeners are exposed as cold Flows via `callbackFlow` in `FirestoreRepository`.

Contributing
- To contribute, open an issue or fork the repository and submit a pull request. Keep changes focused and include tests when applicable.

Security
- Do not commit sensitive Firebase credentials or production `google-services.json` for public repositories. Rotate keys if they were accidentally published.

License
- This project does not include an explicit license file. Add a suitable license (for example MIT) in a `LICENSE` file if you intend to open source this code.

Acknowledgements
- Built with Kotlin, Jetpack Compose, and Firebase Firestore.

