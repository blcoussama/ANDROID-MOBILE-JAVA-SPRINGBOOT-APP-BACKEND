# 📱 AndroidManifest.xml

## 🎯 Rôle du AndroidManifest.xml

C'est le **certificat d'identité** de votre application Android. Il déclare:
- 🔐 **Permissions** nécessaires (Internet, Localisation, etc.)
- 📱 **Toutes les activités** (écrans) de l'app
- 🚪 **Point d'entrée** (première activité au lancement)
- 🎨 **Thème** global
- 📡 **Navigation** entre écrans

**Sans ce fichier = App ne compile pas!** ⚠️

---

## 📦 Section 1: Permissions

```xml
<!-- Permission Internet (OBLIGATOIRE pour Retrofit) -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 🔐 INTERNET

**Rôle:** Autorise l'app à se connecter à Internet

**Sans ça:**
- ❌ Retrofit ne peut pas appeler le backend
- ❌ Toutes les requêtes échouent silencieusement

**Impact utilisateur:** Lors de l'installation, Android demande: *"Cette app accède à Internet. Autoriser?"*

### 🔐 ACCESS_NETWORK_STATE

**Rôle:** Vérifie si Internet est disponible (WiFi/4G)

**Utilisation potentielle:**
```java
// Vérifier connexion avant appel API
if (isNetworkAvailable()) {
    apiService.getAllDoctors();
} else {
    showError("Pas de connexion Internet");
}
```

---

## 🏢 Section 2: Configuration Application

```xml
<application
    android:icon="@mipmap/ic_launcher"           <!-- Icône app -->
    android:label="@string/app_name"             <!-- Nom "Cabinet Médical" -->
    android:theme="@style/Theme.CabinetMedical"  <!-- Thème global -->
    android:usesCleartextTraffic="true"          <!-- HTTP autorisé -->
    ...>
```

### 🎨 android:theme

**Valeur:** `@style/Theme.CabinetMedical`

**Important:** Ce thème utilise **`NoActionBar`** par défaut (c'est pourquoi on a ajouté MaterialToolbar partout!)

### 🔓 android:usesCleartextTraffic="true" ⚠️

**Rôle:** Autorise les connexions **HTTP** (non-sécurisées)

**Pourquoi c'est là:**
```
Backend: http://172.25.135.62:8080  ← HTTP (pas HTTPS)
```

**⚠️ Sécurité:** En production, changez en **HTTPS** et retirez cette ligne!

---

## 📱 Section 3: Activités

**Total: 20 activités déclarées**

### 🚀 Activité LAUNCHER (Point d'Entrée)

```xml
<!-- SplashActivity - LAUNCHER (premier écran) -->
<activity
    android:name=".ui.auth.SplashActivity"
    android:exported="true"
    android:theme="@style/Theme.AppCompat.NoActionBar">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

#### 🔑 Attributs Importants

**`android:exported="true"`**
- ✅ Seule activité qui doit être `true`
- Signifie: "Le launcher Android peut ouvrir cette activité"
- Toutes les autres = `false` (pour sécurité)

**`<intent-filter>` avec MAIN + LAUNCHER**
- Dit à Android: **"C'est l'activité de démarrage!"**
- Icône de l'app dans le menu = ouvre cette activité

**Flux au lancement:**
```
Utilisateur clique icône "Cabinet Médical"
         ↓
SplashActivity s'ouvre
         ↓
Vérifie token JWT dans SharedPreferences
         ↓
Si valide → HomePatientActivity/HomeDoctorActivity/DashboardAdminActivity
Si invalide → LoginActivity
```

---

### 🔗 Navigation Hiérarchique (parentActivityName)

**Exemple: Parcours Patient pour réserver un RDV**

```xml
<activity android:name=".ui.patient.DoctorListActivity"
    android:parentActivityName=".ui.patient.HomePatientActivity" />
          ↓
<activity android:name=".ui.patient.AvailableTimeSlotsActivity"
    android:parentActivityName=".ui.patient.DoctorListActivity" />
          ↓
<activity android:name=".ui.patient.BookAppointmentActivity"
    android:parentActivityName=".ui.patient.AvailableTimeSlotsActivity" />
```

**Hiérarchie de navigation:**
```
HomePatientActivity (Home)
    └─→ DoctorListActivity (Liste médecins)
         └─→ AvailableTimeSlotsActivity (Créneaux)
              └─→ BookAppointmentActivity (Confirmation)
```

**Impact du bouton retour:**
- Depuis BookAppointmentActivity → retourne à AvailableTimeSlotsActivity
- Depuis AvailableTimeSlotsActivity → retourne à DoctorListActivity
- Depuis DoctorListActivity → retourne à HomePatientActivity

**Note:** On a **override** ce comportement avec MaterialToolbar dans certaines activités!

---

### 🏷️ Labels (Titres)

```xml
<activity android:name=".ui.doctor.DoctorTimeSlotsActivity"
    android:label="Mes créneaux horaires" />
```

**Rôle:** Titre affiché dans la barre de titre (si ActionBar visible)

**Dans votre cas:** MaterialToolbar avec `app:title="..."` override ce label

---

## 📊 Résumé des Activités par Rôle

### 🔐 Auth (3 activités)

| Activité | Exported | Parent | Rôle |
|----------|----------|--------|------|
| SplashActivity | ✅ true | - (Root) | Point d'entrée, validation token JWT |
| LoginActivity | ❌ false | - | Connexion utilisateur |
| RegisterActivity | ❌ false | - | Inscription patient |

---

### 👤 Patient (5 activités)

| Activité | Parent | Rôle |
|----------|--------|------|
| HomePatientActivity | - (Home) | Dashboard patient |
| DoctorListActivity | HomePatientActivity | Liste des médecins disponibles |
| AvailableTimeSlotsActivity | DoctorListActivity | Créneaux horaires d'un médecin |
| BookAppointmentActivity | AvailableTimeSlotsActivity | Confirmation et prise de RDV |
| MyAppointmentsActivity | HomePatientActivity | Historique des rendez-vous |

**Parcours typique:**
```
Home → Voir médecins → Choisir médecin → Voir créneaux → Réserver → Mes RDV
```

---

### 👨‍⚕️ Doctor (5 activités)

| Activité | Parent | Rôle |
|----------|--------|------|
| HomeDoctorActivity | - (Home) | Dashboard médecin |
| DoctorAppointmentsActivity | HomeDoctorActivity | Liste des RDV à confirmer/gérer |
| DoctorTimeSlotsActivity | HomeDoctorActivity | Liste des créneaux horaires configurés |
| DoctorAddTimeSlotActivity | DoctorTimeSlotsActivity | Formulaire ajout créneau |
| DoctorEditTimeSlotActivity | DoctorTimeSlotsActivity | Formulaire modification créneau |

**Fonctionnalités principales:**
- Gérer ses rendez-vous (confirmer/annuler)
- Configurer ses créneaux horaires (CRUD)

---

### 👔 Admin (10 activités)

#### Gestion Médecins

| Activité | Parent | Rôle |
|----------|--------|------|
| DashboardAdminActivity | - (Home) | Dashboard avec statistiques |
| AdminDoctorsListActivity | DashboardAdminActivity | Liste de tous les médecins |
| AdminDoctorDetailsActivity | AdminDoctorsListActivity | Détails d'un médecin |
| AdminAddDoctorActivity | AdminDoctorsListActivity | Formulaire ajout médecin |
| AdminEditDoctorActivity | AdminDoctorDetailsActivity | Formulaire modification médecin |

#### Gestion Patients

| Activité | Parent | Rôle |
|----------|--------|------|
| AdminPatientsListActivity | DashboardAdminActivity | Liste de tous les patients |
| AdminPatientDetailsActivity | AdminPatientsListActivity | Détails d'un patient |
| AdminAddPatientActivity | AdminPatientsListActivity | Formulaire ajout patient |
| AdminEditPatientActivity | AdminPatientDetailsActivity | Formulaire modification patient |

#### Supervision Rendez-vous

| Activité | Parent | Rôle |
|----------|--------|------|
| AdminAllAppointmentsActivity | DashboardAdminActivity | Vue globale de tous les RDV |

**Parcours CRUD Médecin:**
```
Dashboard → Liste médecins → Détails médecin → Modifier médecin
                          ↘ Ajouter médecin
```

---

## 🎓 Points Clés à Retenir

### 1. Manifest = Déclaration Obligatoire
- Toute activité DOIT être déclarée ici
- Oubli = Crash au lancement: `ActivityNotFoundException`

### 2. Une Seule LAUNCHER Activity
- SplashActivity = Point d'entrée unique
- `<intent-filter>` avec MAIN + LAUNCHER

### 3. Permissions Critiques
- **INTERNET** = Indispensable pour Retrofit
- **ACCESS_NETWORK_STATE** = Vérifier connexion
- Déclarées une seule fois au début

### 4. parentActivityName = Navigation
- Définit hiérarchie parent-enfant
- Bouton back système en dépend
- Peut être override dans le code Java

### 5. usesCleartextTraffic = HTTP Autorisé
- ⚠️ Seulement pour développement (backend local)
- Production = HTTPS uniquement + supprimer cette ligne

---

## 🔍 Différence: Manifest vs Code Java

| Aspect | AndroidManifest.xml | Code Java (Activity) |
|--------|---------------------|----------------------|
| **Déclare** | Que l'activité existe | Comment l'activité fonctionne |
| **Navigation** | Hiérarchie parent-enfant | Intent, finish(), startActivity() |
| **Thème** | Thème par défaut | Peut être changé au runtime |
| **Permissions** | Demande à l'utilisateur | Vérifie et utilise |
| **Titre** | Label statique | Peut être dynamique (toolbar.setTitle()) |

**Analogie:**
- **Manifest** = Plan d'architecte (structure, organisation)
- **Code Java** = Construction réelle (logique, comportement)

---

## ⚠️ Erreurs Courantes

### 1. Activité Non Déclarée
```
Error: ActivityNotFoundException
```
**Solution:** Ajouter `<activity android:name=".VotreActivity" />` dans le Manifest

### 2. Plusieurs LAUNCHER
```
Warning: Multiple activities with LAUNCHER category
```
**Solution:** Une seule activité doit avoir `<intent-filter>` MAIN + LAUNCHER

### 3. exported=true Sur Toutes Les Activités
**Problème:** Risque de sécurité (autres apps peuvent ouvrir vos activités)
**Solution:** Seule la LAUNCHER doit être `true`, reste = `false`

### 4. Oublier usesCleartextTraffic
```
Error: Cleartext HTTP traffic not permitted
```
**Solution:** Ajouter `android:usesCleartextTraffic="true"` (dev uniquement)

---

## 📋 Checklist de Vérification

✅ Permissions INTERNET déclarées
✅ Une seule activité LAUNCHER (SplashActivity)
✅ Toutes les activités déclarées
✅ parentActivityName correctement défini
✅ usesCleartextTraffic=true (dev) ou HTTPS (prod)
✅ Aucune activité exported=true sauf LAUNCHER

---

## 🔗 Lien avec build.gradle.kts

| build.gradle.kts | AndroidManifest.xml |
|------------------|---------------------|
| Déclare les bibliothèques | Utilise les permissions |
| Configure le thème global | Applique le thème |
| Définit versionCode/Name | Visible dans "À propos" |
| applicationId | Package de base |

**Les deux fichiers travaillent ensemble** pour configurer l'application!
