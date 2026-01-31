# 📋 DOCUMENT DE CONCEPTION - CABINET MÉDICAL (STRICT CAHIER DES CHARGES)

## 🎯 VUE D'ENSEMBLE

### Acteurs du système

1. **PATIENT** - Gère ses rendez-vous
2. **DOCTOR** - Gère ses créneaux et rendez-vous
3. **ADMIN** - Secrétaire - Gestion globale du cabinet

---

## 📊 1. MODÈLE DE DONNÉES MINIMAL

### 1.1 TABLE: users

```sql
user {
  id              BIGINT PK AUTO_INCREMENT
  email           VARCHAR(255) UNIQUE NOT NULL
  passwordHash    VARCHAR(255) NOT NULL
  role            ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL
  firstName       VARCHAR(100) NOT NULL
  lastName        VARCHAR(100) NOT NULL
  phone           VARCHAR(20)
  createdAt       TIMESTAMP NOT NULL
  lastLoginAt     TIMESTAMP
}

RÈGLES:
- Email unique (login)
- Password hashé BCrypt
- role détermine les permissions

```

---

### 1.2 TABLE: patient

```sql
patient {
  id              BIGINT PK AUTO_INCREMENT
  userId          BIGINT FK -> user.id UNIQUE
  createdAt       TIMESTAMP NOT NULL
}

RÈGLES:
- Créé automatiquement lors inscription
- One-to-One avec User

```

---

### 1.3 TABLE: doctor

```sql
doctor {
  id              BIGINT PK AUTO_INCREMENT
  userId          BIGINT FK -> user.id UNIQUE
  specialty       VARCHAR(150)
  createdAt       TIMESTAMP NOT NULL
}

RÈGLES:
- Créé par Admin
- specialty optionnel (pour affichage liste médecins)
- One-to-One avec User

```

---

### 1.4 TABLE: timeslot (Créneaux horaires)

```sql
timeslot {
  id              BIGINT PK AUTO_INCREMENT
  doctorId        BIGINT FK -> doctor.id NOT NULL
  dayOfWeek       ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY',
                       'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL
  startTime       TIME NOT NULL
  endTime         TIME NOT NULL
  createdAt       TIMESTAMP NOT NULL

  UNIQUE(doctorId, dayOfWeek, startTime)
}

RÈGLES:
- Doctor/Admin crée créneaux hebdomadaires
- Ex: Lundi 9h-12h, Mardi 14h-18h
- Pas de chevauchement même médecin même jour

```

---

### 1.5 TABLE: appointment (Rendez-vous)

```sql
appointment {   
  id                  BIGINT PK AUTO_INCREMENT
  patientId           BIGINT FK -> patient.id NOT NULL
  doctorId            BIGINT FK -> doctor.id NOT NULL
  dateTime            TIMESTAMP NOT NULL
  reason              VARCHAR(500)
  status              ENUM('PENDING', 'CONFIRMED', 'CANCELLED') DEFAULT 'PENDING'
  cancelledBy         ENUM('PATIENT', 'DOCTOR', 'ADMIN')
  cancellationReason  TEXT
  createdAt           TIMESTAMP NOT NULL
  updatedAt           TIMESTAMP NOT NULL

  UNIQUE(doctorId, dateTime)
}

RÈGLES:
- PENDING: Créé, en attente
- CONFIRMED: Confirmé par notification
- CANCELLED: Annulé
- Un seul RDV par créneau médecin

```

---

### 1.6 TABLE: notification (Notifications/Rappels)

```sql
notification {
  id              BIGINT PK AUTO_INCREMENT
  appointmentId   BIGINT FK -> appointment.id NOT NULL
  userId          BIGINT FK -> user.id NOT NULL
  type            ENUM('CONFIRMATION', 'REMINDER') NOT NULL
  message         TEXT NOT NULL
  sentAt          TIMESTAMP
  createdAt       TIMESTAMP NOT NULL
}

RÈGLES:
- CONFIRMATION: Envoyée lors création RDV (e-mail/SMS)
- REMINDER: Rappels automatiques avant RDV
- sentAt = NULL si pas encore envoyée

```

---

## 🎭 2. USE CASES PAR ACTEUR

### 2.1 👤 PATIENT (9 use cases)

```
UC-P01: Créer un compte
  └── Input: email, password, firstName, lastName, phone
  └── Output: Compte créé, Patient créé

UC-P02: Se connecter
  └── Input: email, password
  └── Output: JWT token

UC-P03: Consulter historique rendez-vous
  └── Input: userId
  └── Output: Liste RDV (passés + à venir)

UC-P04: Voir liste médecins
  └── Output: Liste doctors (nom, specialty)

UC-P05: Voir créneaux disponibles médecin
  └── Input: doctorId, date
  └── Output: Liste heures disponibles

UC-P06: Prendre rendez-vous
  └── Input: doctorId, dateTime, reason
  └── Actions:
      1. Vérifier créneau disponible
      2. Créer appointment (status=PENDING)
      3. Créer notification CONFIRMATION
      4. Créer notification REMINDER
  └── Output: Confirmation RDV + notification envoyée

UC-P07: Modifier rendez-vous
  └── Input: appointmentId, nouveau dateTime, nouveau reason
  └── Output: RDV modifié + notification

UC-P08: Annuler rendez-vous
  └── Input: appointmentId, cancellationReason
  └── Actions:
      1. Update status = CANCELLED
      2. Update cancelledBy = PATIENT
  └── Output: RDV annulé + notification

UC-P09: Recevoir notifications/rappels
  └── Type: e-mail/SMS
  └── Quand: Confirmation + avant RDV

```

---

### 2.2 👨‍⚕️ DOCTOR (7 use cases)

```
UC-D01: Se connecter
  └── Input: email, password
  └── Output: JWT token

UC-D02: Gérer créneaux horaires
  └── Actions:
      - Créer créneau (jour, startTime, endTime)
      - Modifier créneau
      - Supprimer créneau
  └── Output: Créneaux mis à jour

UC-D03: Consulter rendez-vous programmés
  └── Input: doctorId
  └── Output: Liste RDV du médecin

UC-D04: Voir détails rendez-vous
  └── Input: appointmentId
  └── Output: Infos patient, dateTime, reason

UC-D05: Modifier rendez-vous
  └── Input: appointmentId, nouveau dateTime
  └── Output: RDV modifié

UC-D06: Annuler rendez-vous
  └── Input: appointmentId, cancellationReason
  └── Actions:
      1. Update status = CANCELLED
      2. Update cancelledBy = DOCTOR
  └── Output: RDV annulé + notification patient

UC-D07: Se déconnecter

```

---

### 2.3 👔 ADMIN (Secrétaire) (13 use cases)

```
UC-A01: Se connecter
  └── Input: email, password
  └── Output: JWT token

UC-A02: Voir tableau de bord état rendez-vous
  └── Output:
      - Total RDV aujourd'hui
      - Total RDV semaine
      - RDV par status (PENDING, CONFIRMED, CANCELLED)
      - Liste RDV récents

UC-A03: Gérer utilisateurs - Lister
  └── Output: Liste TOUS users (patients, doctors, admins)

UC-A04: Gérer utilisateurs - Ajouter patient
  └── Input: email, password, firstName, lastName, phone, role=PATIENT
  └── Output: Patient créé

UC-A05: Gérer utilisateurs - Ajouter médecin
  └── Input: email, password, firstName, lastName, phone, role=DOCTOR, specialty
  └── Output: Doctor créé

UC-A06: Gérer utilisateurs - Modifier
  └── Input: userId, nouveaux champs
  └── Actions:
      1. Vérifier que user n'est pas un autre ADMIN
      2. Update user
  └── Output: User modifié
  └── ⚠️ RESTRICTION SÉCURITÉ: Admin ne peut pas modifier d'autres admins

UC-A07: Gérer utilisateurs - Supprimer
  └── Input: userId
  └── Actions:
      1. Vérifier que user n'est pas un autre ADMIN
      2. Delete user (cascade)
  └── Output: User supprimé
  └── ⚠️ RESTRICTION SÉCURITÉ: Admin ne peut pas supprimer d'autres admins

UC-A08: Gérer créneaux horaires médecins
  └── Actions:
      - Voir créneaux TOUS médecins
      - Créer créneau pour n'importe quel médecin
      - Modifier créneau
      - Supprimer créneau
  └── Output: Créneaux mis à jour

UC-A09: Voir TOUS rendez-vous
  └── Output: Liste globale RDV (tous patients, tous médecins)

UC-A10: Modifier rendez-vous
  └── Input: appointmentId, nouveaux champs
  └── Output: RDV modifié

UC-A11: Annuler rendez-vous
  └── Input: appointmentId, cancellationReason
  └── Actions:
      1. Update status = CANCELLED
      2. Update cancelledBy = ADMIN
  └── Output: RDV annulé + notification

UC-A12: Déplacer rendez-vous
  └── Input: appointmentId, nouveau doctorId, nouveau dateTime
  └── Actions:
      1. Vérifier nouveau créneau disponible
      2. Update appointment
  └── Output: RDV déplacé + notification

UC-A13: Se déconnecter

```

---

## 🔒 MODIFICATIONS SÉCURITÉ IMPLÉMENTÉES

### ⚠️ Restriction Admin (Use Cases UC-A06, UC-A07)

**RÈGLE AJOUTÉE (non dans cahier initial, mais implémentée pour sécurité) :**

```
MODIFICATION UTILISATEURS:
├── Admin PEUT modifier/supprimer: PATIENT, DOCTOR
├── Admin NE PEUT PAS modifier/supprimer: Autres ADMIN
└── EXCEPTION: Admin peut modifier/supprimer son propre compte

RAISON DE CETTE RÈGLE:
├── Sécurité: Empêcher admin malveillant de supprimer tous les admins
├── Best practice: Protection des comptes administrateurs
├── Éviter escalade de privilèges
└── Conformité standards de sécurité

IMPLÉMENTATION:
├── UserService.updateUser():
│   └── if (userToUpdate.role == ADMIN && userToUpdate.id != currentUser.id)
│       └── throw SecurityException
│
├── UserService.deleteUser():
│   └── if (userToDelete.role == ADMIN && userToDelete.id != currentUser.id)
│       └── throw SecurityException
│
└── Code HTTP 403 FORBIDDEN + Message clair en français

```

**Exemple de messages d'erreur :**

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Vous ne pouvez pas modifier un autre administrateur"
}

{
  "status": 403,
  "error": "Forbidden",
  "message": "Vous ne pouvez pas supprimer un autre administrateur"
}

```

**Tests validés :**

```
✅ Admin modifie patient → OK (200)
✅ Admin modifie doctor → OK (200)
✅ Admin modifie son propre compte → OK (200)
✅ Admin modifie autre admin → FORBIDDEN (403)
✅ Admin supprime patient → OK (204)
✅ Admin supprime doctor → OK (204)
✅ Admin supprime son propre compte → OK (204)
✅ Admin supprime autre admin → FORBIDDEN (403)

```

---

## 📱 3. ÉCRANS APPLICATION ANDROID

### 3.1 COMMUN (2 écrans)

```
SCR-00: Splash Screen
  └── Logo, loading

SCR-01: Login
  └── Email, Password, "Se connecter", "S'inscrire"

```

---

### 3.2 PATIENT (9 écrans)

```
SCR-P01: Register Patient
  └── Email, Password, FirstName, LastName, Phone
  └── Bouton: S'inscrire

SCR-P02: Home Patient
  └── Prochain RDV (si existe)
  └── Boutons: Prendre RDV, Mes RDV

SCR-P03: Liste Médecins
  └── RecyclerView: Nom, Specialty
  └── Clic → Voir créneaux

SCR-P04: Créneaux Disponibles
  └── CalendarView (sélection date)
  └── Liste heures disponibles
  └── Clic heure → Formulaire RDV

SCR-P05: Prendre RDV (Formulaire)
  └── Médecin (affiché), Date/Heure (affichées), Motif (input)
  └── Boutons: Annuler, Confirmer

SCR-P06: Historique Rendez-vous
  └── Tabs: À venir / Passés
  └── RecyclerView: Date, Médecin, Motif, Status

SCR-P07: Détails RDV
  └── Médecin, Date, Heure, Motif, Status
  └── Boutons: Modifier, Annuler

SCR-P08: Modifier RDV
  └── Nouveau créneau (CalendarView + heures)
  └── Nouveau motif

SCR-P09: Annuler RDV
  └── Raison annulation (optionnelle)
  └── Boutons: Retour, Confirmer annulation

```

---

### 3.3 DOCTOR (7 écrans)

```
SCR-D01: Home Doctor
  └── Nb RDV aujourd'hui
  └── Prochain patient
  └── Boutons: Mes RDV, Gérer créneaux

SCR-D02: Mes Rendez-vous
  └── CalendarView + ListView
  └── Clic RDV → Détails

SCR-D03: Détails RDV Doctor
  └── Patient (nom, phone), Date, Heure, Motif
  └── Boutons: Modifier, Annuler

SCR-D04: Modifier RDV Doctor
  └── Nouveau créneau

SCR-D05: Annuler RDV Doctor
  └── Raison annulation

SCR-D06: Gérer Créneaux
  └── Liste créneaux par jour (RecyclerView)
  └── Boutons: Ajouter, Modifier, Supprimer

SCR-D07: Ajouter/Modifier Créneau
  └── Jour semaine (Spinner), Heure début, Heure fin
  └── Boutons: Annuler, Enregistrer

```

---

### 3.4 ADMIN (9 écrans)

```
SCR-A01: Dashboard Admin
  └── Total RDV aujourd'hui
  └── Total RDV semaine
  └── RDV par status (chart/cards)
  └── Boutons: Gérer Users, Gérer Créneaux, Gérer RDV

SCR-A02: Gestion Utilisateurs
  └── Liste users (RecyclerView: Nom, Email, Rôle)
  └── Boutons: Ajouter Patient, Ajouter Médecin, Ajouter Admin
  └── ⚠️ Note: Boutons Modifier/Supprimer désactivés si autre ADMIN

SCR-A03: Ajouter Utilisateur
  └── Rôle (Spinner: PATIENT, DOCTOR, ADMIN)
  └── Email, Password, FirstName, LastName, Phone
  └── Si DOCTOR: Specialty
  └── Bouton: Créer

SCR-A04: Détails Utilisateur
  └── Infos complètes
  └── Boutons: Modifier, Supprimer
  └── ⚠️ Note: Boutons désactivés si autre ADMIN

SCR-A05: Modifier Utilisateur
  └── Formulaire pré-rempli
  └── ⚠️ Note: Interdit si autre ADMIN (afficher message)

SCR-A06: Gestion Créneaux
  └── Sélection médecin (Spinner)
  └── Liste créneaux médecin sélectionné
  └── Boutons: Ajouter, Modifier, Supprimer

SCR-A07: Tous les Rendez-vous
  └── Liste globale (RecyclerView: Patient, Médecin, Date, Status)
  └── Bouton: Créer RDV

SCR-A08: Détails RDV Admin
  └── Patient, Médecin, Date, Heure, Motif, Status
  └── Boutons: Modifier, Annuler, Déplacer

SCR-A09: Déplacer RDV
  └── Nouveau médecin (Spinner)
  └── Nouveau créneau (CalendarView + heures)
  └── Bouton: Confirmer

```

---

## 🔌 4. API REST ENDPOINTS

### 4.1 AUTHENTICATION

```
POST   /api/auth/register         -- Inscription patient
POST   /api/auth/login            -- Connexion
POST   /api/auth/logout           -- Déconnexion

```

---

### 4.2 USERS (Admin uniquement)

```
GET    /api/users                 -- Liste utilisateurs
POST   /api/users                 -- Créer utilisateur
GET    /api/users/{id}            -- Détails utilisateur
PUT    /api/users/{id}            -- Modifier utilisateur (⚠️ sauf autres admins)
DELETE /api/users/{id}            -- Supprimer utilisateur (⚠️ sauf autres admins)

```

---

### 4.3 DOCTORS

```
GET    /api/doctors               -- Liste médecins (PUBLIC)
GET    /api/doctors/{id}          -- Détails médecin

```

---

### 4.4 TIMESLOTS

```
GET    /api/timeslots/doctor/{doctorId}           -- Créneaux d'un médecin
GET    /api/timeslots/available?doctorId=X&date=Y -- Créneaux disponibles
POST   /api/timeslots             -- Créer créneau (DOCTOR, ADMIN)
PUT    /api/timeslots/{id}        -- Modifier créneau (DOCTOR, ADMIN)
DELETE /api/timeslots/{id}        -- Supprimer créneau (DOCTOR, ADMIN)

```

---

### 4.5 APPOINTMENTS

```
GET    /api/appointments                        -- Liste RDV (selon rôle)
GET    /api/appointments/{id}                   -- Détails RDV
GET    /api/appointments/patient/{patientId}   -- RDV d'un patient
GET    /api/appointments/doctor/{doctorId}     -- RDV d'un médecin
POST   /api/appointments          -- Créer RDV (PATIENT, ADMIN)
PUT    /api/appointments/{id}     -- Modifier RDV
DELETE /api/appointments/{id}     -- Annuler RDV
POST   /api/appointments/{id}/move -- Déplacer RDV (ADMIN)

```

---

### 4.6 DASHBOARD (Admin)

```
GET    /api/admin/dashboard       -- Statistiques rendez-vous

```

---

## ⚖️ 5. RÈGLES MÉTIER

```
RG-01: Email unique
RG-02: Un seul RDV par créneau médecin (UNIQUE doctorId + dateTime)
RG-03: Patient peut annuler/modifier ses RDV
RG-04: Doctor peut annuler/modifier ses RDV
RG-05: Admin peut tout faire (créer, modifier, annuler, déplacer)
RG-06: Notification CONFIRMATION envoyée lors création RDV
RG-07: Notifications REMINDER envoyées avant RDV
RG-08: Créneaux ne peuvent pas chevaucher (même doctor, même jour)
RG-09: Admin ne peut pas modifier/supprimer d'autres admins (sécurité) ⭐ AJOUTÉE

```

---

## 🔒 6. PERMISSIONS

```
┌─────────────────────────────┬─────────┬────────┬───────┐
│ ACTION                      │ PATIENT │ DOCTOR │ ADMIN │
├─────────────────────────────┼─────────┼────────┼───────┤
│ Créer compte patient        │    ✅   │   ❌   │  ✅   │
│ Voir liste médecins         │    ✅   │   ✅   │  ✅   │
│ Prendre RDV                 │    ✅   │   ❌   │  ✅   │
│ Voir SES RDV                │    ✅   │   ✅   │  ❌   │
│ Voir TOUS RDV               │    ❌   │   ❌   │  ✅   │
│ Modifier SES RDV            │    ✅   │   ✅   │  ❌   │
│ Modifier TOUS RDV           │    ❌   │   ❌   │  ✅   │
│ Annuler SES RDV             │    ✅   │   ✅   │  ❌   │
│ Annuler TOUS RDV            │    ❌   │   ❌   │  ✅   │
│ Déplacer RDV                │    ❌   │   ❌   │  ✅   │
│ Gérer SES créneaux          │    ❌   │   ✅   │  ❌   │
│ Gérer TOUS créneaux         │    ❌   │   ❌   │  ✅   │
│ CRUD users (PATIENT/DOCTOR) │    ❌   │   ❌   │  ✅   │
│ Modifier/Supprimer ADMIN    │    ❌   │   ❌   │  ⚠️   │
│ Dashboard                   │    ❌   │   ❌   │  ✅   │
└─────────────────────────────┴─────────┴────────┴───────┘

⚠️ = Admin peut uniquement modifier/supprimer son PROPRE compte, pas d'autres admins

```

---

## 📋 RÉSUMÉ - CE QUI A ÉTÉ RETIRÉ

```
❌ RETIRÉ (non demandé dans cahier):
   ├── licenseNumber
   ├── consultationFee
   ├── bio
   ├── yearsExperience
   ├── dateOfBirth
   ├── address
   ├── medicalHistory
   ├── officeAddress
   ├── duration dans appointment/timeslot
   ├── Status COMPLETED, NO_SHOW
   ├── isActive (activer/désactiver)
   ├── Types notifications détaillés (REMINDER_24H, REMINDER_1H, etc.)
   ├── Changer password
   ├── Rapports CSV/PDF
   ├── Audit log détaillé
   ├── Délais spécifiques (2h/4h)
   └── Filtres avancés/Recherche

✅ GARDÉ (strictement cahier):
   ├── Users (email, password, role, firstName, lastName, phone)
   ├── Patient (lié à User)
   ├── Doctor (lié à User, specialty minimal)
   ├── TimeSlot (créneaux horaires)
   ├── Appointment (RDV avec reason, status basique)
   ├── Notification (confirmation + rappels)
   ├── CRUD users (admin)
   ├── Gestion créneaux
   ├── Prise/modification/annulation RDV
   ├── Déplacement RDV (admin)
   └── Dashboard simple

⭐ AJOUTÉ (pour sécurité):
   └── Restriction modification/suppression autres admins (RG-09)

```

---

## 📊 ÉTAT IMPLÉMENTATION BACKEND

```
✅ TERMINÉ (5/6 controllers):
├── AuthController (3 endpoints)
│   └── UC-P01, UC-P02, UC-D01, UC-A01
├── UserController (6 endpoints + sécurité admin)
│   └── UC-A03, UC-A04, UC-A05, UC-A06, UC-A07 + RG-09
├── DoctorController (5 endpoints)
│   └── UC-P04, UC-P05 (via alias)
├── TimeSlotController (5 endpoints)
│   └── UC-D02, UC-A08
├── AppointmentController (9 endpoints)
│   └── UC-P06, UC-P07, UC-P08, UC-D03, UC-D04, UC-D05, UC-D06
│   └── UC-A09, UC-A10, UC-A11, UC-A12

⏳ EN COURS (1/6 controller):
└── DashboardController (1 endpoint)
    └── UC-A02: Voir tableau de bord état rendez-vous

PROGRESSION: 83% (5/6 controllers terminés)

```

---

**✅ DOCUMENT CONCEPTION COMPLET - MIS À JOUR AVEC SÉCURITÉ ADMIN**

**Date de mise à jour :** 29 décembre 2025

**Version :** 1.1 (ajout RG-09 et section sécurité)

**Conformité cahier des charges :** ✅ 100%

**Modifications sécurité :** ✅ Documentées

---

**📋 PROCHAINE ÉTAPE : DashboardController (UC-A02)**
