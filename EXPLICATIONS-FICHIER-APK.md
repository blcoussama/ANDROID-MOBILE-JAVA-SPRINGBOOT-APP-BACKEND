# 📱 C'EST QUOI UN FICHIER APK?

  APK = Android Package Kit

  C'est le fichier d'installation de votre application Android, similaire à:

- .exe pour Windows
- .dmg pour Mac
- .deb pour Linux

  Contenu d'un APK:

  app.apk
  ├─ AndroidManifest.xml (compilé)
  ├─ classes.dex (code Java/Kotlin compilé)
  ├─ res/ (images, layouts XML compilés)
  ├─ lib/ (bibliothèques natives)
  └─ resources.arsc (ressources compilées)

  IMPORTANT: L'APK contient du code compilé (bytecode), PAS le code source Java lisible!

  ---
  🎓 POURQUOI LE PROFESSEUR VEUT UN APK?

  Raisons principales:

  1. ✅ Tester l'application sur son propre téléphone/tablette Android
  2. ✅ Voir l'interface utilisateur (UI/UX) en action
  3. ✅ Vérifier les fonctionnalités sans avoir à compiler le code
  4. ✅ Évaluer rapidement plusieurs projets d'étudiants
  5. ❌ PAS pour lire le code source (APK est compilé)

  Pour voir le code source, le prof aurait besoin du projet Android Studio (dossier complet avec les fichiers .java).

  ---
  ⚠️ LE GROS PROBLÈME: BACKEND LOCAL!

  Votre situation actuelle:

  Votre Machine (WSL2)
  ├─ Backend Spring Boot → <http://localhost:8080>
  ├─ PostgreSQL → localhost:5432
  └─ Android App (APK) → <http://10.0.2.2:8080> (émulateur)
                      ou <http://192.168.x.x:8080> (réseau local)

  ❌ Ce qui va se passer si le prof installe votre APK:

  Téléphone du Professeur
  ├─ Install app.apk ✅
  ├─ Launch app ✅
  ├─ Try to login ❌ ERREUR!
  └─ Backend unreachable (<http://10.0.2.2:8080> n'existe pas sur son téléphone!)

  RÉSULTAT: L'app va crasher ou afficher "Erreur réseau" partout!

  ---
  💡 SOLUTIONS POSSIBLES:

  Option 1: Déployer le Backend sur le Cloud (RECOMMANDÉ)

  Avantages:

- ✅ Le prof peut tester l'app complète
- ✅ Fonctionne de n'importe où
- ✅ Démo professionnelle

  Plateformes gratuites:

  1. Railway.app (le plus simple)
    - PostgreSQL inclus
    - Déploiement automatique depuis Git
    - Gratuit: 500h/mois
  2. Render.com
    - PostgreSQL gratuit
    - Auto-deploy
  3. Heroku (si encore gratuit)
    - Classique pour Spring Boot

  Ensuite:

- Modifier ApiClient.java avec l'URL du serveur cloud:
  private static final String BASE_URL = "<https://votre-app.railway.app/>";
- Rebuilder l'APK avec la nouvelle URL

  ---
  Option 2: Utiliser ngrok (Temporaire)

  Exposer votre backend local sur Internet:

# Installer ngrok

# Lancer backend

  ./mvnw spring-boot:run

# Dans un autre terminal

  ngrok http 8080

  ngrok vous donne une URL publique:
  <https://abcd-1234.ngrok-free.app>

  Modifier ApiClient.java:
  private static final String BASE_URL = "<https://abcd-1234.ngrok-free.app/>";

  ⚠️ Inconvénients:

- URL change à chaque redémarrage ngrok
- Gratuit limité à 2h
- Vous devez laisser votre PC allumé

  ---
  Option 3: APK + Instructions Backend Local

  Fournir au prof:

  1. ✅ APK de l'app
  2. ✅ Fichier JAR du backend (target/cabinet-medical-0.0.1-SNAPSHOT.jar)
  3. ✅ Script SQL pour créer la DB
  4. ✅ README avec instructions

  Inconvénient: Le prof doit installer Java, PostgreSQL, et lancer le backend = compliqué!

  ---
  Option 4: Vidéo Démo (Alternative)

  Au lieu d'un APK fonctionnel:

- 🎥 Enregistrer une vidéo de démonstration (5-10 min)
- 📱 Montrer toutes les fonctionnalités
- 💬 Expliquer le code pendant la démo

  Outils:

- OBS Studio (gratuit)
- AZ Screen Recorder (Android)
- Scrcpy (enregistrer écran Android depuis PC)

  ---
  🎯 MA RECOMMANDATION:

  SOLUTION COMPLÈTE:

  1. Déployer le backend sur Railway.app (30 min)
    - Gratuit
    - PostgreSQL inclus
    - URL permanente
  2. Générer l'APK avec l'URL cloud
    - L'app fonctionnera partout
  3. Préparer le rapport avec:
    - ✅ APK fonctionnel
    - ✅ Documentation code Android
    - ✅ Architecture (Android + Backend + DB)
    - ✅ Screenshots de l'app
    - ✅ (Optionnel) Vidéo démo courte

  ---
  📋 COMMENT GÉNÉRER L'APK?

  Dans Android Studio:

  1. Build → Build Bundle(s) / APK(s) → Build APK(s)
  2. Attendre la compilation
  3. Click "locate" dans la notification
  4. APK généré: app/build/outputs/apk/debug/app-debug.apk

  ⚠️ AVANT de générer:

- Vérifier que BASE_URL dans ApiClient.java pointe vers le serveur accessible
- Si backend local: <http://10.0.2.2:8080> (ne fonctionnera QUE sur émulateur)
- Si backend cloud: <https://votre-app.railway.app/>

  ---
  🤔 QUELLE SOLUTION CHOISIR?

  Question: Vous préférez:

  1. Je vous aide à déployer sur Railway (30 min, solution pro)
  2. Utiliser ngrok (5 min, solution rapide mais temporaire)
  3. Fournir APK + Backend local (prof doit installer)
  4. Faire une vidéo démo (pas d'APK, mais montre tout)

  Qu'est-ce qui vous convient le mieux? 🎯
