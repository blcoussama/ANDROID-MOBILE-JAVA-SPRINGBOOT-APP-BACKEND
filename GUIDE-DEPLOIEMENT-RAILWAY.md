# 🚀 Guide Complet de Déploiement sur Railway.app

**Date:** 2026-02-01
**Projet:** Cabinet Médical - Backend Spring Boot + PostgreSQL
**URL Production:** https://android-mobile-java-springboot-app-backend-production.up.railway.app

---

## 📋 Table des Matières

1. [Préparation du Projet](#1-préparation-du-projet)
2. [Configuration des Variables d'Environnement](#2-configuration-des-variables-denvironnement)
3. [Déploiement sur Railway](#3-déploiement-sur-railway)
4. [Configuration de la Base de Données](#4-configuration-de-la-base-de-données)
5. [Tests et Vérification](#5-tests-et-vérification)
6. [Création des Comptes Initiaux](#6-création-des-comptes-initiaux)
7. [Configuration de l'Application Android](#7-configuration-de-lapplication-android)
8. [Dépannage](#8-dépannage)

---

## 1. Préparation du Projet

### 1.1 Nettoyage du Dépôt GitHub

**Objectif:** Retirer les fichiers de documentation du dépôt Git tout en les conservant localement.

#### Mise à jour du `.gitignore`

```bash
# Ajouter au .gitignore
### Documentation et fichiers d'apprentissage (ne pas pousser en production) ###
EXPLICATIONS/
AVANCEMENTS/
IMAGES/
EXPLICATIONS-FICHIER-APK.md
SYSTEME-RESERVATION-EXPLICATION.md
CONCEPTION.md
ANDROID-CONCEPTION-PLANNING.md
IMPLEMENTATION_ADMIN_PATIENTS_RDV.md
IMPLEMENTATION_TIMESLOTS_DOCTOR.md
CAT-LOGS.md
BACKEND-SERVER-LOGS.md
.claude/
*.swp
*.swo
*~
```

#### Retirer les fichiers du tracking Git

```bash
# Retirer les fichiers de Git sans les supprimer localement
git rm -r --cached EXPLICATIONS/
git rm -r --cached AVANCEMENTS/
git rm -r --cached IMAGES/
git rm --cached EXPLICATIONS-FICHIER-APK.md
git rm --cached SYSTEME-RESERVATION-EXPLICATION.md
git rm --cached CONCEPTION.md
git rm --cached ANDROID-CONCEPTION-PLANNING.md
git rm --cached IMPLEMENTATION_ADMIN_PATIENTS_RDV.md
git rm --cached IMPLEMENTATION_TIMESLOTS_DOCTOR.md
git rm --cached CAT-LOGS.md
git rm --cached BACKEND-SERVER-LOGS.md

# Commit et push
git add .gitignore
git commit -m "docs: Update .gitignore to exclude documentation files"
git push origin main
```

**Résultat:** Les fichiers restent sur votre ordinateur mais ne sont plus sur GitHub.

---

### 1.2 Configuration des Variables d'Environnement avec `.env`

**Objectif:** Séparer les secrets du code source pour la sécurité.

#### Installation de la Dépendance

Ajout dans `pom.xml`:

```xml
<!-- Spring Dotenv - Pour charger les variables .env -->
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

#### Création du Fichier `.env` (Local)

```bash
# Créer le fichier .env à la racine du projet
DATABASE_URL=jdbc:postgresql://localhost:5432/cabinet_medical
PGUSER=cabinetmed_user
PGPASSWORD=cabinetmed123
PORT=8080
JWT_SECRET=LocalDevelopmentSecretKeyForJWT2025ChangeInProduction
JWT_EXPIRATION=3600000
```

**⚠️ Important:** Le fichier `.env` est dans `.gitignore` - il ne sera JAMAIS commité sur GitHub.

#### Création du Fichier `.env.example` (Template)

```bash
# Fichier .env.example - À commiter sur GitHub
DATABASE_URL=jdbc:postgresql://localhost:5432/your_database_name
PGUSER=your_database_user
PGPASSWORD=your_database_password
PORT=8080
JWT_SECRET=your_strong_random_secret_key_here_minimum_32_characters
JWT_EXPIRATION=3600000
```

#### Modification de `application.properties`

```properties
# ===================================
# DATABASE POSTGRESQL
# ===================================
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# ===================================
# JPA / HIBERNATE
# ===================================
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ===================================
# SERVER
# ===================================
server.port=${PORT}
server.address=0.0.0.0

# ===================================
# JWT (JSON Web Token)
# ===================================
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
```

**Note:** Les variables utilisent `${VAR}` sans valeurs par défaut pour forcer l'utilisation d'environnement variables.

#### Commit des Changements

```bash
git add pom.xml application.properties .env.example .gitignore
git commit -m "feat: Add .env configuration for environment variables"
git push origin main
```

---

## 2. Configuration des Variables d'Environnement

### 2.1 Environnement Local

Les variables sont chargées automatiquement depuis le fichier `.env` par la dépendance `spring-dotenv`.

### 2.2 Environnement Production (Railway)

Les variables sont configurées manuellement dans Railway (voir section 4).

---

## 3. Déploiement sur Railway

### 3.1 Création du Compte

1. Aller sur **https://railway.app**
2. Cliquer sur **"Start a New Project"** ou **"Login with GitHub"**
3. Autoriser Railway à accéder à vos dépôts GitHub

### 3.2 Création du Projet

1. Cliquer sur **"New Project"**
2. Sélectionner **"Deploy from GitHub repo"**
3. Choisir le dépôt: `android-mobile-java-springboot-app-backend`
4. Railway commence le build automatiquement

### 3.3 Ajout de PostgreSQL

1. Dans le projet Railway, cliquer sur **"+ New"**
2. Sélectionner **"Database"** → **"Add PostgreSQL"**
3. Railway crée automatiquement une base de données PostgreSQL
4. Noter les variables générées (PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD)

---

## 4. Configuration de la Base de Données

### 4.1 Variables d'Environnement Railway

**⚠️ IMPORTANT:** Railway génère l'URL PostgreSQL au format `postgresql://...` mais Spring Boot nécessite `jdbc:postgresql://...`

#### Configuration dans le Service Backend

Aller dans: **Projet → Service Backend → Variables**

Ajouter les variables suivantes:

| Variable | Valeur |
|----------|--------|
| `DATABASE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `PGUSER` | `${{Postgres.PGUSER}}` |
| `PGPASSWORD` | `${{Postgres.POSTGRES_PASSWORD}}` |
| `JWT_SECRET` | `RailwayProductionSecretKey2026CabinetMedicalStrongRandomJWTToken` |
| `JWT_EXPIRATION` | `3600000` |

**Explication:**
- `${{Postgres.VARIABLE}}` : Référence les variables du service PostgreSQL
- `jdbc:postgresql://` : Préfixe requis par Spring Boot JDBC Driver
- JWT_SECRET : Généré avec `openssl rand -base64 32`

#### Génération d'un JWT Secret Sécurisé

```bash
openssl rand -base64 32
```

### 4.2 Redéploiement

Après avoir ajouté les variables:
1. Railway redéploie automatiquement
2. Vérifier les logs dans **Deployments → View Logs**

**Logs de succès attendus:**

```
:: Spring Boot :: (v3.5.0)
...
Bootstrapping Spring Data JPA repositories in DEFAULT mode.
...
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...
Initialized JPA EntityManagerFactory for persistence unit 'default'
...
Started CabinetMedicalBackendApplication in 8.123 seconds
```

### 4.3 Vérification des Tables

Hibernate crée automatiquement les tables suivantes:
- `users` (utilisateurs)
- `doctor` (profils médecins)
- `patient` (profils patients)
- `timeslot` (créneaux horaires)
- `appointment` (rendez-vous)

**Vérification dans Railway:**
1. Aller dans **Postgres → Database → Data**
2. Vous devriez voir les 5 tables créées

---

## 5. Tests et Vérification

### 5.1 Génération du Domaine Public

1. Aller dans **Service Backend → Settings → Networking**
2. Cliquer sur **"Generate Domain"**
3. Railway génère une URL publique: `android-mobile-java-springboot-app-backend-production.up.railway.app`

### 5.2 Test du Backend

#### Test de l'Endpoint Racine

```bash
curl https://android-mobile-java-springboot-app-backend-production.up.railway.app
```

**Réponse attendue:** HTTP 403 Forbidden (normal, Spring Security protège la route)

```json
{
  "timestamp": "2026-02-01T18:45:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/"
}
```

#### Test de l'Endpoint de Santé

```bash
curl https://android-mobile-java-springboot-app-backend-production.up.railway.app/actuator/health
```

**Réponse attendue:**

```json
{
  "status": "UP"
}
```

---

## 6. Création des Comptes Initiaux

### 6.1 Problème Initial

L'application nécessite un compte ADMIN pour créer des médecins et des patients, mais il n'y a pas d'interface pour créer le premier admin.

**Solution:** Insérer manuellement un compte admin dans la base de données.

### 6.2 Accès à la Base de Données Railway

**Via l'Interface Web:**
1. Aller dans **Postgres → Database → Data**
2. Cliquer sur la table `users`
3. Cliquer sur **"+ Row"**

### 6.3 Génération des Hash BCrypt

Les mots de passe doivent être hashés avec BCrypt avant insertion.

**Générer les hash:**

```bash
python3 -c "import bcrypt; print('admin123:', bcrypt.hashpw(b'admin123', bcrypt.gensalt()).decode()); print('doctor123:', bcrypt.hashpw(b'doctor123', bcrypt.gensalt()).decode()); print('patient123:', bcrypt.hashpw(b'patient123', bcrypt.gensalt()).decode())"
```

**Résultat (exemple):**

```
admin123: $2b$12$sNTnmPkpKqWN88/Poz2Moee4GXtnq6vT7K0BvQTIvako2.wE3lJim
doctor123: $2b$12$WhKu8R5LHeqLKmt4phFzguFQh0.syLaL.noOFSdP3xMHHdEfzanKO
patient123: $2b$12$StADsVXhotolRvPb894IMu1qwiaSqh6mumBxyE0q9y/T2yDa6ZO5u
```

### 6.4 Insertion du Compte Admin

**Dans Railway → Postgres → Table `users` → "+ Row":**

| Colonne | Valeur |
|---------|---------|
| `id` | (Laisser vide ou NULL - auto-généré) |
| `email` | `admin@cabinet.ma` |
| `passwordHash` | `$2b$12$sNTnmPkpKqWN88/Poz2Moee4GXtnq6vT7K0BvQTIvako2.wE3lJim` |
| `firstName` | `Admin` |
| `lastName` | `Cabinet` |
| `phone` | `0612345678` |
| `role` | `ADMIN` |
| `createdAt` | `NOW()` ou `2026-02-01 19:00:00` |
| `lastLoginAt` | (Laisser vide) |

**⚠️ Note:** Si Railway demande un ID, mettez `1`. Si une erreur se produit, le compteur d'auto-increment peut sauter des IDs (normal).

### 6.5 Création des Autres Comptes

Une fois le compte admin créé, vous pouvez créer des médecins et patients **directement via l'application Android** en vous connectant avec le compte admin.

---

## 7. Configuration de l'Application Android

### 7.1 Mise à Jour de l'URL Backend

**Fichier:** `app/src/main/java/com/cabinet/cabinetmedical/api/ApiClient.java`

```java
public class ApiClient {
    // ✅ URL Production Railway
    private static final String BASE_URL = "https://android-mobile-java-springboot-app-backend-production.up.railway.app/";

    // ...
}
```

### 7.2 Compilation de l'APK

**Dans Android Studio:**
1. **Build → Generate App Bundles or APKs → Build APK(s)**
2. Attendre la compilation
3. Fichier généré: `app/build/outputs/apk/debug/app-debug.apk`

### 7.3 Installation sur Android

1. Transférer `app-debug.apk` sur le téléphone Android
2. Autoriser l'installation depuis "Sources inconnues" si demandé
3. Installer l'APK
4. Lancer l'application

### 7.4 Test de Connexion

**Compte Admin:**
- Email: `admin@cabinet.ma`
- Password: `admin123`

**Fonctionnalités à tester:**
- ✅ Connexion
- ✅ Créer un médecin
- ✅ Créer un patient
- ✅ Se déconnecter et se reconnecter avec le compte médecin
- ✅ Gérer les créneaux (médecin)
- ✅ Créer un rendez-vous (patient)

---

## 8. Dépannage

### 8.1 Erreur: "Could not resolve placeholder 'DATABASE_URL'"

**Cause:** Variables d'environnement manquantes dans Railway.

**Solution:** Vérifier que toutes les variables sont configurées dans **Backend Service → Variables**.

---

### 8.2 Erreur: "Driver org.postgresql.Driver claims to not accept jdbcUrl, postgresql://..."

**Cause:** Format d'URL incorrect (manque le préfixe `jdbc:`).

**Solution:** S'assurer que `DATABASE_URL` commence par `jdbc:postgresql://` et non `postgresql://`.

---

### 8.3 Backend Crash: "HikariPool - Exception during pool initialization"

**Cause:** Impossible de se connecter à PostgreSQL.

**Solutions:**
1. Vérifier que le service PostgreSQL est bien démarré
2. Vérifier les credentials (PGUSER, PGPASSWORD)
3. Vérifier que DATABASE_URL utilise les bonnes références: `${{Postgres.PGHOST}}`

---

### 8.4 Erreur 403 Forbidden sur Tous les Endpoints

**Cause:** Spring Security protège tous les endpoints.

**Solution:** C'est normal pour la route racine `/`. Tester `/actuator/health` ou les endpoints API avec un token JWT valide.

---

### 8.5 Application Android Ne Se Connecte Pas

**Causes possibles:**
1. URL incorrecte dans `ApiClient.java`
2. Backend non démarré sur Railway
3. Problème réseau sur le téléphone

**Vérifications:**
```bash
# Tester depuis le téléphone Android
curl https://android-mobile-java-springboot-app-backend-production.up.railway.app/actuator/health
```

---

## 9. Informations de Facturation Railway

### 9.1 Plan Gratuit

**Trial de 30 jours:**
- $5 de crédit gratuit par mois
- Suffisant pour un backend léger + PostgreSQL
- Pas besoin de carte bancaire pendant le trial

**Vérification de l'usage:**
1. **Railway → Settings → Billing**
2. Voir les crédits utilisés et jours restants

### 9.2 Annulation

Pour annuler avant la fin du trial:
1. **Settings → Billing → Cancel Subscription**
2. Ou simplement supprimer le projet

---

## 10. Credentials pour le Professeur

### Fichier `CREDENTIALS.txt` à Fournir

```
=== CREDENTIALS DE TEST - CABINET MEDICAL ===

URL Backend: https://android-mobile-java-springboot-app-backend-production.up.railway.app

--- COMPTE ADMINISTRATEUR ---
Email: admin@cabinet.ma
Password: admin123
Rôle: Gérer médecins et patients

--- COMPTE MÉDECIN ---
Email: docteur@cabinet.ma
Password: doctor123
Rôle: Gérer créneaux et rendez-vous

--- COMPTE PATIENT ---
Email: patient@cabinet.ma
Password: patient123
Rôle: Prendre des rendez-vous

=== INSTRUCTIONS ===
1. Installer le fichier APK sur Android
2. Lancer l'application
3. Se connecter avec un des comptes ci-dessus
4. Tester les fonctionnalités selon le rôle
```

---

## 11. Architecture Finale

```
┌─────────────────────────────────────────────────┐
│           APPLICATION ANDROID (APK)              │
│  - LoginActivity                                 │
│  - DashboardAdminActivity (ADMIN)                │
│  - HomeDoctorActivity (DOCTOR)                   │
│  - HomePatientActivity (PATIENT)                 │
└──────────────────┬──────────────────────────────┘
                   │
                   │ HTTPS (Retrofit)
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│      RAILWAY.APP - BACKEND SERVICE               │
│  - Spring Boot 3.5.0                             │
│  - Spring Security + JWT                         │
│  - API REST Controllers                          │
│  - URL: android-mobile-java-springboot-...       │
└──────────────────┬──────────────────────────────┘
                   │
                   │ JDBC
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│      RAILWAY.APP - POSTGRESQL SERVICE            │
│  - 5 Tables (users, doctor, patient,             │
│    timeslot, appointment)                        │
│  - Auto-gérée par Hibernate                      │
└─────────────────────────────────────────────────┘
```

---

## 12. Checklist Finale

**Avant de remettre le projet:**

- [x] Backend déployé sur Railway
- [x] PostgreSQL configuré et tables créées
- [x] Variables d'environnement configurées
- [x] Domaine public généré
- [x] Compte admin créé dans la base de données
- [x] APK compilé et testé sur Android
- [x] ApiClient.java mis à jour avec URL production
- [x] Tests de connexion réussis (admin, doctor, patient)
- [x] Fichier CREDENTIALS.txt créé pour le professeur
- [x] Documentation complète disponible

---

## 📞 Support

**En cas de problème:**
1. Vérifier les logs Railway: **Deployments → View Logs**
2. Vérifier les variables d'environnement
3. Tester les endpoints avec `curl`
4. Vérifier la connexion réseau du téléphone Android

---

**🎉 Déploiement Réussi!**

Backend en ligne: https://android-mobile-java-springboot-app-backend-production.up.railway.app
Date de déploiement: 2026-02-01
Status: ✅ Opérationnel
