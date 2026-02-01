# 📦 build.gradle.kts (Module: app)

## 🎯 Rôle de ce Fichier

Ce fichier est la **configuration centrale** de votre application Android. Il définit:
- Les versions Android supportées
- Toutes les bibliothèques externes utilisées
- Les paramètres de compilation

---

## 📦 Section 1: Plugins

```kotlin
plugins {
    alias(libs.plugins.android.application)
}
```

**Explication:** Active le plugin Android pour transformer ce projet en application Android compilable.

---

## ⚙️ Section 2: Configuration Android

### Identité de l'App

```kotlin
namespace = "com.cabinet.cabinetmedical"      // Package Java de base
applicationId = "com.cabinet.cabinetmedical"  // ID unique sur Google Play
versionCode = 1                               // Version interne (incrémenté à chaque release)
versionName = "1.0"                          // Version affichée aux utilisateurs
```

### Versions SDK

```kotlin
minSdk = 24        // Android 7.0 (2016) = version minimale supportée
targetSdk = 35     // Android 14 = version ciblée (optimisations)
compileSdk = 36    // SDK utilisé pour compiler
```

**Impact:** L'app fonctionne sur **~95% des appareils Android** en circulation (Android 7.0+)

### Compilation Java

```kotlin
sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17
```

**Explication:** Utilise Java 17 (lambdas, streams, etc.)

---

## 📚 Section 3: Dépendances

### 🔹 Groupe 1: Android de Base

```kotlin
implementation(libs.appcompat)           // Compatibilité avec anciennes versions Android
implementation(libs.material)            // Material Design (boutons, cards, etc.)
implementation(libs.activity)            // Gestion des activités
implementation(libs.constraintlayout)    // Layouts flexibles
```

### 🔹 Groupe 2: RETROFIT - Communication avec le Backend ⭐

#### Retrofit Core
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
```

**Rôle:** Transforme les appels HTTP en méthodes Java simples

**Exemple:**
```java
// Au lieu de:
HttpURLConnection conn = new URL("http://...").openConnection();

// On fait:
apiService.getAllDoctors()
```

#### Gson Converter
```kotlin
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

**Rôle:** Convertit automatiquement JSON ↔ objets Java

**Exemple:**
```java
// JSON du backend → DoctorResponse (objet Java)
```

#### OkHttp - Client HTTP
```kotlin
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

**Rôle:** Le **vrai client HTTP** qui fait les requêtes réseau
- Utilisé **EN COULISSE** par Retrofit
- Gère: connexions, timeout, cache, cookies, etc.

**Relation:**
```
Retrofit → utilise → OkHttp → fait les vraies requêtes HTTP
```

**Sans OkHttp**, Retrofit ne pourrait pas fonctionner! C'est le moteur qui fait tourner Retrofit.

#### Logging Interceptor
```kotlin
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

**Rôle:** Affiche toutes les requêtes HTTP dans Logcat (debug uniquement)

**Exemple de sortie:**
```
GET http://172.25.135.62:8080/api/doctors
Response: 200 OK [{"id":1,"firstName":"Ahmed",...}]
```

### 🔹 Groupe 3: GSON - Manipulation JSON

```kotlin
implementation("com.google.code.gson:gson:2.10.1")
```

**Rôle:** Parse et génère du JSON (utilisé avec ErrorParser)

#### 🤔 Pourquoi Deux Dépendances GSON?

| Bibliothèque | Créée Par | Rôle | Utilisée Où? |
|--------------|-----------|------|--------------|
| **gson** (2.10.1) | Google | Convertit JSON ↔ Java | ErrorParser, et indirectement partout via Retrofit |
| **converter-gson** (2.9.0) | Square | Connecte GSON à Retrofit | ApiClient (configuration Retrofit) |

**Flux complet:**

```
┌─────────────────────────────────────────────────────────┐
│  ANDROID APP                                            │
│  apiService.getAllDoctors()  ← Vous appelez            │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  RETROFIT                                               │
│  "Je fais l'appel HTTP GET /api/doctors"               │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  BACKEND SPRING BOOT                                    │
│  Renvoie: [{"id":1,"firstName":"Ahmed",...}]           │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  RETROFIT-GSON CONVERTER                                │
│  "Hey GSON! Convertis ce JSON en List<DoctorResponse>" │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  GSON CORE                                              │
│  JSON String → List<DoctorResponse> (objets Java)      │
└─────────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────────┐
│  ANDROID APP                                            │
│  List<DoctorResponse> doctors = response.body();       │
└─────────────────────────────────────────────────────────┘
```

**Utilisation directe de GSON dans ErrorParser.java:**
```java
String errorJson = errorBody.string(); // JSON brut du backend
Gson gson = new Gson();
ErrorResponse error = gson.fromJson(errorJson, ErrorResponse.class);
```

### 🔹 Groupe 4: RecyclerView - Listes

```kotlin
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
```

**Rôle:** Affiche des listes performantes (médecins, RDV, patients, créneaux)

#### RecyclerView - C'est Quoi?

**RecyclerView** = Composant Android pour afficher des **listes** de manière **ultra-performante**

**Principe du "Recyclage":**
```
Écran visible: Affiche 5 items
Liste totale: 100 médecins

┌─────────────────┐
│ Médecin 1      │ ← Vue créée
│ Médecin 2      │ ← Vue créée
│ Médecin 3      │ ← Vue créée  } Seulement 5-7 vues
│ Médecin 4      │ ← Vue créée    en mémoire!
│ Médecin 5      │ ← Vue créée
└─────────────────┘
  ↓ Scroll vers le bas

La vue "Médecin 1" (disparue) est RECYCLÉE pour afficher "Médecin 6"!
```

**Performance:**
- **Sans RecyclerView:** 100 vues créées = 💥 Crash ou lag
- **Avec RecyclerView:** 5-7 vues recyclées = 🚀 Fluide

**Utilisé dans votre projet:**
- **Patient:** DoctorListActivity, AvailableTimeSlotsActivity, MyAppointmentsActivity
- **Doctor:** DoctorAppointmentsActivity, DoctorTimeSlotsActivity
- **Admin:** AdminDoctorsListActivity, AdminPatientsListActivity, AdminAllAppointmentsActivity

**Architecture RecyclerView (3 composants):**
1. Le Layout XML (RecyclerView)
2. L'Adapter (Classe Java qui relie données aux vues)
3. Le Layout de l'Item (Design d'un élément)

#### CardView - C'est Quoi?

**CardView** = Le **design visuel** des items dans vos listes

C'est un **rectangle avec:**
- 🔲 Coins arrondis
- 🌑 Ombre (élévation)
- 📦 Padding automatique

**Exemple:**
```xml
<CardView>  ← Rectangle avec ombre et coins arrondis
    <TextView>Dr. Ahmed</TextView>
    <TextView>Cardiologue</TextView>
</CardView>
```

**Résultat:** Items séparés, effet "cartes empilées" 🃏

### 🔹 Groupe 5: Material Components

```kotlin
implementation("com.google.android.material:material:1.11.0")
```

**Rôle:** Composants Material Design 3

**Composants utilisés dans votre projet:**

| Composant | Utilisé Où |
|-----------|------------|
| **MaterialToolbar** | Toutes les 17 activités (header avec bouton retour) |
| **MaterialButton** | BookAppointmentActivity, DoctorAddTimeSlotActivity, etc. |
| **TextInputLayout** | Tous les formulaires (Login, Register, Add Doctor, etc.) |
| **FloatingActionButton** | AdminDoctorsListActivity, DoctorTimeSlotsActivity (bouton +) |
| **MaterialCardView** | Item layouts (item_doctor.xml, item_appointment.xml, etc.) |

**Sans cette bibliothèque:** Pas de look Material Design moderne ❌

---

## 🧪 Section 4: Tests

```kotlin
testImplementation(libs.junit)
androidTestImplementation(libs.ext.junit)
androidTestImplementation(libs.espresso.core)
```

**Types de tests:**

| Test | Rôle | Exemple |
|------|------|---------|
| **JUnit** | Tests unitaires (logique pure) | Tester validation mot de passe |
| **AndroidX JUnit** | Tests Android (avec contexte) | Tester SharedPreferences |
| **Espresso** | Tests UI (simule utilisateur) | Tester login complet |

**Utilisé dans le projet?** Non, aucun test écrit actuellement

**À enlever?** Non! Ne prennent pas de place dans l'APK final. Standards dans tout projet Android.

---

## 📊 Résumé Visuel

| Bibliothèque | Version | Rôle Principal |
|-------------|---------|----------------|
| **Retrofit** | 2.9.0 | Appels API REST |
| **Gson** | 2.10.1 | JSON ↔ Java |
| **OkHttp** | 4.12.0 | Client HTTP bas niveau |
| **RecyclerView** | 1.3.2 | Listes optimisées |
| **Material** | 1.11.0 | UI moderne |

---

## 🎓 Points Clés à Retenir

1. **Retrofit + OkHttp + Gson = Trio de Communication Backend**
   - Retrofit = Interface haut niveau
   - OkHttp = Moteur HTTP
   - Gson = Convertisseur JSON

2. **RecyclerView = Performance des Listes**
   - Réutilise les vues au scroll (économie mémoire)
   - Indispensable pour listes longues

3. **Material Components = UI Professionnelle**
   - Design cohérent avec les apps Google
   - Animations et transitions fluides

4. **Tests Inclus Mais Non Utilisés**
   - Pas d'impact sur l'APK final
   - Bonne pratique de les garder
