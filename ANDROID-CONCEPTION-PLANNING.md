## 🎯 **OBJECTIF**

Développer l'application mobile Android pour le système de gestion de cabinet médical avec 3 interfaces utilisateur (Patient, Doctor, Admin).

---

## 📊 **VUE D'ENSEMBLE**

### **Application à développer**

```
ANDROID APP - CABINET MÉDICAL

ACTEURS: 3
├── Patient (9 écrans)
├── Doctor (7 écrans)
└── Admin (9 écrans)

TOTAL: 27 écrans + 2 communs (Login, Register)
TOTAL GÉNÉRAL: 29 écrans

TECHNOLOGIE:
├── Android Studio
├── Java
├── Retrofit (API REST)
├── JWT Authentication
└── Material Design

```

---

## 📋 **ÉCRANS À DÉVELOPPER**

### **🔐 COMMUN (2 écrans)**

### **SCR-00: Splash Screen**

```
Fonctionnalités:
└── Logo application
└── Chargement initial
└── Vérification token JWT
└── Redirection automatique:
    ├── Si token valide → Home (selon rôle)
    └── Si pas de token → Login

Durée: 30 minutes

```

### **SCR-01: Login**

```
Fonctionnalités:
└── Email (input)
└── Password (input masqué)
└── Bouton "Se connecter"
└── Lien "S'inscrire" → Register
└── Appel API: POST /api/auth/login
└── Stockage token JWT
└── Redirection selon rôle:
    ├── PATIENT → Home Patient
    ├── DOCTOR → Home Doctor
    └── ADMIN → Dashboard Admin

Durée: 2 heures

```

---

### **👤 PATIENT (9 écrans)**

### **SCR-P01: Register Patient**

```
Fonctionnalités:
└── Email, Password, FirstName, LastName, Phone
└── Validation formulaire
└── Bouton "S'inscrire"
└── Appel API: POST /api/auth/register
└── Auto-login après inscription

Durée: 1.5 heures

```

### **SCR-P02: Home Patient**

```
Fonctionnalités:
└── Message bienvenue (firstName)
└── Prochain RDV (si existe)
└── Boutons:
    ├── "Prendre RDV"
    ├── "Mes RDV"
    └── "Se déconnecter"

Durée: 2 heures

```

### **SCR-P03: Liste Médecins**

```
Fonctionnalités:
└── RecyclerView liste médecins
└── Affichage: Nom, Spécialité
└── Filtre par spécialité (optionnel)
└── Clic médecin → Créneaux disponibles
└── Appel API: GET /api/doctors

Durée: 2 heures

```

### **SCR-P04: Créneaux Disponibles**

```
Fonctionnalités:
└── CalendarView (sélection date)
└── Liste heures disponibles pour date choisie
└── Affichage créneaux du médecin
└── Clic heure → Formulaire RDV
└── Appel API: GET /api/timeslots/available

Durée: 3 heures

```

### **SCR-P05: Prendre RDV (Formulaire)**

```
Fonctionnalités:
└── Médecin (affiché, non modifiable)
└── Date/Heure (affichées, non modifiables)
└── Motif consultation (input)
└── Boutons: "Annuler", "Confirmer"
└── Appel API: POST /api/appointments

Durée: 1.5 heures

```

### **SCR-P06: Historique Rendez-vous**

```
Fonctionnalités:
└── Tabs: "À venir" / "Passés"
└── RecyclerView: Date, Médecin, Motif, Status
└── Couleurs selon status:
    ├── PENDING → Orange
    ├── CONFIRMED → Vert
    └── CANCELLED → Rouge
└── Clic RDV → Détails RDV
└── Appel API: GET /api/appointments/patient/{id}

Durée: 3 heures

```

### **SCR-P07: Détails RDV**

```
Fonctionnalités:
└── Médecin (nom, spécialité)
└── Date, Heure
└── Motif
└── Status
└── Boutons (si status = PENDING ou CONFIRMED):
    ├── "Modifier"
    └── "Annuler"

Durée: 2 heures

```

### **SCR-P08: Modifier RDV**

```
Fonctionnalités:
└── Nouveau créneau (CalendarView + heures)
└── Nouveau motif (input)
└── Boutons: "Retour", "Enregistrer"
└── Appel API: PUT /api/appointments/{id}

Durée: 2 heures

```

### **SCR-P09: Annuler RDV**

```
Fonctionnalités:
└── Raison annulation (input optionnel)
└── Boutons: "Retour", "Confirmer annulation"
└── Dialog confirmation
└── Appel API: POST /api/appointments/{id}/cancel

Durée: 1 heure

```

**TOTAL PATIENT: ~18 heures (1.5 jours)**

---

### **👨‍⚕️ DOCTOR (7 écrans)**

### **SCR-D01: Home Doctor**

```
Fonctionnalités:
└── Nb RDV aujourd'hui
└── Prochain patient (nom, heure)
└── Boutons:
    ├── "Mes RDV"
    ├── "Gérer créneaux"
    └── "Se déconnecter"

Durée: 2 heures

```

### **SCR-D02: Mes Rendez-vous**

```
Fonctionnalités:
└── CalendarView
└── Liste RDV du jour sélectionné
└── Affichage: Patient, Heure, Motif
└── Clic RDV → Détails RDV
└── Appel API: GET /api/appointments/doctor/{id}

Durée: 3 heures

```

### **SCR-D03: Détails RDV Doctor**

```
Fonctionnalités:
└── Patient (nom, téléphone)
└── Date, Heure
└── Motif
└── Status
└── Boutons:
    ├── "Modifier"
    └── "Annuler"

Durée: 2 heures

```

### **SCR-D04: Modifier RDV Doctor**

```
Fonctionnalités:
└── Nouveau créneau (date + heure)
└── Boutons: "Retour", "Enregistrer"
└── Appel API: PUT /api/appointments/{id}

Durée: 1.5 heures

```

### **SCR-D05: Annuler RDV Doctor**

```
Fonctionnalités:
└── Raison annulation (input)
└── Boutons: "Retour", "Confirmer"
└── Dialog confirmation
└── Appel API: POST /api/appointments/{id}/cancel

Durée: 1 heure

```

### **SCR-D06: Gérer Créneaux**

```
Fonctionnalités:
└── Liste créneaux par jour (RecyclerView)
└── Affichage: Jour, Heure début, Heure fin
└── Boutons: "Ajouter", "Modifier", "Supprimer"
└── Appel API: GET /api/timeslots/doctor/{id}

Durée: 2 heures

```

### **SCR-D07: Ajouter/Modifier Créneau**

```
Fonctionnalités:
└── Jour semaine (Spinner: Lundi-Dimanche)
└── Heure début (TimePicker)
└── Heure fin (TimePicker)
└── Validation (fin > début)
└── Boutons: "Annuler", "Enregistrer"
└── Appel API: POST/PUT /api/timeslots

Durée: 2 heures

```

**TOTAL DOCTOR: ~13.5 heures (1 jour)**

---

### **👔 ADMIN (9 écrans)**

### **SCR-A01: Dashboard Admin**

```
Fonctionnalités:
└── Total RDV aujourd'hui
└── Total RDV semaine
└── RDV par status (chart/cards)
└── Boutons:
    ├── "Gérer Utilisateurs"
    ├── "Gérer Créneaux"
    └── "Gérer RDV"
└── Appel API: GET /api/admin/dashboard

Durée: 3 heures

```

### **SCR-A02: Gestion Utilisateurs**

```
Fonctionnalités:
└── Liste users (RecyclerView)
└── Affichage: Nom, Email, Rôle
└── Boutons: "Ajouter Patient", "Ajouter Médecin"
└── Clic user → Détails utilisateur
└── Appel API: GET /api/users

Durée: 2 heures

```

### **SCR-A03: Ajouter Utilisateur**

```
Fonctionnalités:
└── Rôle (Spinner: PATIENT, DOCTOR, ADMIN)
└── Email, Password, FirstName, LastName, Phone
└── Si DOCTOR: Specialty (input)
└── Bouton "Créer"
└── Appel API: POST /api/users

Durée: 2 heures

```

### **SCR-A04: Détails Utilisateur**

```
Fonctionnalités:
└── Infos complètes user
└── Boutons: "Modifier", "Supprimer"
└── Dialog confirmation suppression

Durée: 1.5 heures

```

### **SCR-A05: Modifier Utilisateur**

```
Fonctionnalités:
└── Formulaire pré-rempli
└── Modification tous champs sauf email
└── Boutons: "Annuler", "Enregistrer"
└── Appel API: PUT /api/users/{id}

Durée: 1.5 heures

```

### **SCR-A06: Gestion Créneaux (Admin)**

```
Fonctionnalités:
└── Sélection médecin (Spinner)
└── Liste créneaux médecin sélectionné
└── Boutons: "Ajouter", "Modifier", "Supprimer"
└── Appel API: GET /api/doctors + GET /api/timeslots/doctor/{id}

Durée: 2.5 heures

```

### **SCR-A07: Tous les Rendez-vous**

```
Fonctionnalités:
└── Liste globale RDV (RecyclerView)
└── Affichage: Patient, Médecin, Date, Status
└── Filtre par status (optionnel)
└── Clic RDV → Détails RDV Admin
└── Appel API: GET /api/appointments

Durée: 2.5 heures

```

### **SCR-A08: Détails RDV Admin**

```
Fonctionnalités:
└── Patient (nom, email, téléphone)
└── Médecin (nom, spécialité)
└── Date, Heure, Motif, Status
└── Boutons:
    ├── "Modifier"
    ├── "Annuler"
    └── "Déplacer"

Durée: 2 heures

```

### **SCR-A09: Déplacer RDV**

```
Fonctionnalités:
└── Nouveau médecin (Spinner)
└── Nouveau créneau (CalendarView + heures)
└── Bouton "Confirmer"
└── Appel API: POST /api/appointments/{id}/move

Durée: 2.5 heures

```

**TOTAL ADMIN: ~19.5 heures (1.5 jours)**

---
