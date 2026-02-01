# 📱 COMPRÉHENSION COMPLÈTE DU CODE ANDROID

## 📋 TABLE DES MATIÈRES

1. [Architecture Globale](#architecture-globale)
2. [Flow d'Authentification](#flow-dauthentification)
3. [Models (Request & Response)](#models-request--response)
4. [API Layer](#api-layer)
5. [Utils (Utilitaires)](#utils-utilitaires)
6. [UI Patient](#ui-patient)
7. [UI Doctor](#ui-doctor)
8. [UI Admin](#ui-admin)
9. [Adapters (RecyclerView)](#adapters-recyclerview)
10. [Flows Complets](#flows-complets)

---

## 🏗️ ARCHITECTURE GLOBALE

### Structure du Projet

```
app/src/main/java/com/cabinet/cabinetmedical/
│
├── api/                    # Communication avec le backend
│   ├── ApiClient.java     # Configuration Retrofit + OkHttp
│   └── ApiService.java    # Définition endpoints REST
│
├── model/                  # Classes de données (DTOs)
│   ├── request/           # Requêtes envoyées au backend
│   └── response/          # Réponses reçues du backend
│
├── ui/                     # Interface utilisateur (Activities)
│   ├── auth/              # Authentification (Login, Register, Splash)
│   ├── patient/           # Écrans patient (5 activities)
│   ├── doctor/            # Écrans médecin (5 activities)
│   └── admin/             # Écrans admin (9 activities + 2 adapters)
│
├── utils/                  # Classes utilitaires
│   ├── ErrorParser.java   # Parser les erreurs backend
│   └── JwtInterceptor.java # Ajouter token JWT automatiquement
│
└── MainActivity.java       # Activity temporaire (fallback)
```

### Technologies Utilisées

- **Retrofit 2.9.0**: Client HTTP pour appels API REST
- **OkHttp 4.10.0**: Gestion requêtes HTTP + intercepteurs (logs, JWT)
- **Gson 2.10.1**: Sérialisation/Désérialisation JSON ↔ Java
- **Material Components 1.9.0**: Composants UI Material Design 3
- **RecyclerView**: Affichage listes performantes

---

## 🔐 FLOW D'AUTHENTIFICATION

### 1. Démarrage de l'App

**SplashActivity.java** (Premier écran, `android.intent.category.LAUNCHER`)

```java
// RÔLE: Écran de chargement (2 secondes) qui vérifie si l'utilisateur est déjà connecté

onCreate() {
    // 1. Attendre 2 secondes
    new Handler().postDelayed(() -> {
        checkAuthentication(); // 2. Vérifier token JWT
    }, 2000);
}

checkAuthentication() {
    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
    String token = prefs.getString("jwt_token", null);

    if (token != null && !token.isEmpty()) {
        // Token existe → Valider avec le backend
        apiService.validateToken().enqueue(...);
        // Si valide (200 OK) → navigateToMain()
        // Si expiré (401) → navigateToLogin()
    } else {
        // Pas de token → LoginActivity
        navigateToLogin();
    }
}

navigateToMain() {
    String role = prefs.getString("user_role", "");
    // PATIENT → HomePatientActivity
    // DOCTOR → HomeDoctorActivity
    // ADMIN → DashboardAdminActivity
}
```

**OÙ UTILISÉ**: Point d'entrée de l'app (AndroidManifest: `MAIN` + `LAUNCHER`)

**FLOW VISUEL**:
```
App Launch → SplashActivity (2s)
                    ↓
         Token existe? ──NO──→ LoginActivity
                    ↓ YES
              Valider token
                    ↓
         Valid? ──NO──→ LoginActivity
                    ↓ YES
              Redirection selon rôle:
              • PATIENT → HomePatientActivity
              • DOCTOR → HomeDoctorActivity
              • ADMIN → DashboardAdminActivity
```

---

### 2. Connexion Utilisateur

**LoginActivity.java**

```java
// RÔLE: Formulaire de connexion pour TOUS les rôles (Patient, Doctor, Admin)

attemptLogin() {
    // 1. Récupérer email + password depuis les champs
    String email = emailInput.getText().toString().trim();
    String password = passwordInput.getText().toString().trim();

    // 2. Validation locale
    if (email.isEmpty() || password.isEmpty()) {
        showError("Champs obligatoires");
        return;
    }

    // 3. Créer la requête
    LoginRequest request = new LoginRequest(email, password);

    // 4. Appel API
    apiService.login(request).enqueue(new Callback<LoginResponse>() {
        @Override
        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
            if (response.isSuccessful()) {
                LoginResponse loginResponse = response.body();

                // 5. Sauvegarder dans SharedPreferences
                saveLoginData(loginResponse);

                // 6. Rediriger selon le rôle
                navigateToHome(loginResponse.getRole());
            } else if (response.code() == 401) {
                showError("Email ou mot de passe incorrect");
            }
        }
    });
}

saveLoginData(LoginResponse response) {
    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();

    editor.putString("jwt_token", response.getToken());
    editor.putString("user_email", response.getEmail());
    editor.putString("user_role", response.getRole());
    editor.putLong("user_id", response.getUserId());

    // Selon le rôle, stocker patientId ou doctorId
    if (response.getPatientId() != null) {
        editor.putLong("patient_id", response.getPatientId());
    }
    if (response.getDoctorId() != null) {
        editor.putLong("doctor_id", response.getDoctorId());
    }

    editor.apply();
}
```

**API APPELÉE**: `POST /api/auth/login` → Retourne `LoginResponse`

**OÙ UTILISÉ**:
- Depuis SplashActivity (si pas de token)
- Depuis bouton "Déconnexion" de n'importe quel dashboard

---

### 3. Inscription Patient

**RegisterActivity.java**

```java
// RÔLE: Formulaire d'inscription pour créer un nouveau compte PATIENT

attemptRegister() {
    // 1. Récupérer les données du formulaire
    String email = emailInput.getText().toString().trim();
    String password = passwordInput.getText().toString().trim();
    String firstName = firstNameInput.getText().toString().trim();
    String lastName = lastNameInput.getText().toString().trim();
    String phone = phoneInput.getText().toString().trim();

    // 2. Validation locale
    if (email.isEmpty() || password.length() < 6 || firstName.isEmpty() || lastName.isEmpty()) {
        showError("Tous les champs sont obligatoires");
        return;
    }

    // 3. Créer requête
    RegisterRequest request = new RegisterRequest(email, password, firstName, lastName, phone);

    // 4. Appel API
    apiService.register(request).enqueue(new Callback<LoginResponse>() {
        @Override
        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
            if (response.isSuccessful()) {
                // Inscription réussie → Le backend retourne LoginResponse (auto-connexion)
                LoginResponse loginResponse = response.body();
                saveLoginData(loginResponse);

                // Rediriger vers HomePatientActivity
                Intent intent = new Intent(RegisterActivity.this, HomePatientActivity.class);
                startActivity(intent);
                finish();
            } else if (response.code() == 409) {
                showError("Email déjà utilisé");
            }
        }
    });
}
```

**API APPELÉE**: `POST /api/auth/register` → Retourne `LoginResponse` (compte créé + connexion auto)

**OÙ UTILISÉ**: Depuis LoginActivity (lien "S'inscrire")

---

## 📦 MODELS (REQUEST & RESPONSE)

### Request Models (Requêtes envoyées au backend)

#### **LoginRequest.java**
```java
public class LoginRequest {
    private String email;
    private String password;
}
```
**UTILISÉ PAR**: LoginActivity → `apiService.login(loginRequest)`

---

#### **RegisterRequest.java**
```java
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
}
```
**UTILISÉ PAR**: RegisterActivity → `apiService.register(registerRequest)`

---

#### **CreateAppointmentRequest.java**
```java
public class CreateAppointmentRequest {
    private Long doctorId;
    private String dateTime;  // Format: "2026-02-03T09:00:00"
    private String reason;
}
```
**UTILISÉ PAR**: BookAppointmentActivity → `apiService.createAppointment(patientId, request)`

---

#### **CreateTimeSlotRequest.java**
```java
public class CreateTimeSlotRequest {
    private Long doctorId;
    private String dayOfWeek;    // "MONDAY", "TUESDAY", ...
    private String startTime;    // "09:00:00"
    private String endTime;      // "12:00:00"
}
```
**UTILISÉ PAR**: DoctorAddTimeSlotActivity → `apiService.createTimeSlot(request)`

---

#### **UpdateTimeSlotRequest.java**
```java
public class UpdateTimeSlotRequest {
    private String dayOfWeek;
    private String startTime;
    private String endTime;
}
```
**UTILISÉ PAR**: DoctorEditTimeSlotActivity → `apiService.updateTimeSlot(id, request)`

---

#### **CreateUserRequest.java**
```java
public class CreateUserRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;  // Obligatoire pour DOCTOR, null pour PATIENT
}
```
**UTILISÉ PAR**:
- AdminAddPatientActivity → `apiService.createPatient(request)` (specialty = null)
- AdminAddDoctorActivity → `apiService.createDoctor(request)` (specialty obligatoire)

---

#### **UpdateUserRequest.java**
```java
public class UpdateUserRequest {
    private String email;
    private String password;  // Vide = ne pas modifier
    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;
}
```
**UTILISÉ PAR**:
- AdminEditPatientActivity → `apiService.updateUser(userId, request)`
- AdminEditDoctorActivity → `apiService.updateUser(userId, request)`

---

### Response Models (Réponses reçues du backend)

#### **LoginResponse.java**
```java
public class LoginResponse {
    private String token;      // JWT token
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;       // "PATIENT", "DOCTOR", "ADMIN"
    private Long patientId;    // Si PATIENT
    private Long doctorId;     // Si DOCTOR
}
```
**UTILISÉ PAR**:
- LoginActivity → Sauvegarder données + rediriger selon rôle
- RegisterActivity → Connexion automatique après inscription
- SplashActivity → Valider token

**COMMENT**: Gson désérialise automatiquement le JSON en objet LoginResponse

---

#### **AppointmentResponse.java**
```java
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String dateTime;           // "2026-02-03T14:00:00"
    private String reason;
    private String status;             // "PENDING", "CONFIRMED", "CANCELLED"
    private String cancelledBy;        // "PATIENT", "DOCTOR", "ADMIN"
    private String cancellationReason;

    // Méthodes helper
    public String getFormattedDateTime() {
        // "2026-02-03T14:00:00" → "03/02/2026 à 14:00"
    }

    public String getStatusLabel() {
        // "PENDING" → "En attente"
        // "CONFIRMED" → "Confirmé"
        // "CANCELLED" → "Annulé"
    }
}
```
**UTILISÉ PAR**:
- MyAppointmentsActivity (Patient) → Liste RDV du patient
- DoctorAppointmentsActivity (Doctor) → Liste RDV du médecin
- AdminAllAppointmentsActivity (Admin) → Liste TOUS les RDV
- AppointmentAdapter, DoctorAppointmentAdapter → Affichage dans RecyclerView

---

#### **TimeSlotResponse.java**
```java
public class TimeSlotResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String dayOfWeek;      // "MONDAY"
    private String dayOfWeekFr;    // "Lundi" (traduit par backend)
    private String startTime;      // "09:00:00"
    private String endTime;        // "12:00:00"
    private Integer durationMinutes; // 180

    // Méthodes helper
    public String getTimeRange() {
        // "09:00:00" + "12:00:00" → "09:00 - 12:00"
    }

    public String getFormattedDuration() {
        // 180 minutes → "3h"
        // 90 minutes → "1h 30min"
    }
}
```
**UTILISÉ PAR**:
- DoctorTimeSlotsActivity → Liste créneaux du médecin
- DoctorTimeSlotAdapter → Affichage dans RecyclerView

---

#### **DoctorResponse.java**
```java
public class DoctorResponse {
    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;

    public String getFullName() {
        return "Dr. " + firstName + " " + lastName;
    }

    public String getSpecialtyOrDefault() {
        return specialty != null ? specialty : "Médecin généraliste";
    }
}
```
**UTILISÉ PAR**:
- DoctorListActivity (Patient) → Liste des médecins pour réserver RDV
- AdminDoctorsListActivity (Admin) → Gestion médecins
- DoctorAdapter, AdminDoctorAdapter → Affichage dans RecyclerView

---

#### **UserResponse.java**
```java
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;        // "PATIENT", "DOCTOR", "ADMIN"
    private String specialty;   // Pour DOCTOR
    private String createdAt;
}
```
**UTILISÉ PAR**:
- AdminPatientDetailsActivity → Détails d'un patient
- AdminDoctorDetailsActivity → Détails d'un médecin
- AdminPatientsListActivity, AdminDoctorsListActivity → Listes

---

#### **DashboardResponse.java**
```java
public class DashboardResponse {
    private Long totalAppointmentsToday;
    private Long totalAppointmentsWeek;
    private Map<String, Long> appointmentsByStatus;  // {"PENDING": 2, "CONFIRMED": 1, ...}
    private List<AppointmentResponse> recentAppointments;
    private Long totalDoctors;
    private Long totalPatients;

    // Méthodes helper
    public Long getPendingCount() {
        return appointmentsByStatus.getOrDefault("PENDING", 0L);
    }
}
```
**UTILISÉ PAR**: DashboardAdminActivity → Statistiques admin

---

## 🌐 API LAYER

### **ApiClient.java**

**RÔLE**: Configuration centrale Retrofit + OkHttp pour toutes les requêtes HTTP

```java
public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";  // Émulateur Android
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // 1. Créer OkHttpClient avec intercepteurs
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new JwtInterceptor(context))  // Ajoute token JWT
                .addInterceptor(new HttpLoggingInterceptor()  // Log requêtes/réponses
                    .setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

            // 2. Créer Retrofit avec Gson converter
            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}
```

**UTILISÉ PAR**: TOUTES les Activities qui font des appels API

**EXEMPLE D'UTILISATION**:
```java
// Dans n'importe quelle Activity
ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
Call<LoginResponse> call = apiService.login(loginRequest);
call.enqueue(new Callback<LoginResponse>() { ... });
```

---

### **ApiService.java**

**RÔLE**: Interface Retrofit définissant TOUS les endpoints REST du backend

```java
public interface ApiService {

    // ========== AUTHENTIFICATION ==========

    @POST("api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/auth/validate")
    Call<Map<String, Object>> validateToken();


    // ========== DOCTORS ==========

    @GET("api/doctors")
    Call<List<DoctorResponse>> getAllDoctors();


    // ========== TIMESLOTS ==========

    @GET("api/timeslots/available")
    Call<List<String>> getAvailableTimeSlots(
        @Query("doctorId") Long doctorId,
        @Query("date") String date
    );

    @GET("api/timeslots/doctor/{doctorId}")
    Call<List<TimeSlotResponse>> getDoctorTimeSlots(@Path("doctorId") Long doctorId);

    @POST("api/timeslots")
    Call<TimeSlotResponse> createTimeSlot(@Body CreateTimeSlotRequest request);

    @PUT("api/timeslots/{id}")
    Call<TimeSlotResponse> updateTimeSlot(
        @Path("id") Long timeSlotId,
        @Body UpdateTimeSlotRequest request
    );

    @DELETE("api/timeslots/{id}")
    Call<Void> deleteTimeSlot(@Path("id") Long timeSlotId);


    // ========== APPOINTMENTS ==========

    @POST("api/appointments")
    Call<AppointmentResponse> createAppointment(
        @Query("patientId") Long patientId,
        @Body CreateAppointmentRequest request
    );

    @GET("api/appointments")
    Call<List<AppointmentResponse>> getAllAppointments();  // Admin

    @GET("api/appointments/patient/{patientId}")
    Call<List<AppointmentResponse>> getPatientAppointments(@Path("patientId") Long patientId);

    @GET("api/appointments/doctor/{doctorId}")
    Call<List<AppointmentResponse>> getDoctorAppointments(@Path("doctorId") Long doctorId);

    @POST("api/appointments/{id}/confirm")
    Call<AppointmentResponse> confirmAppointment(@Path("id") Long appointmentId);

    @HTTP(method = "DELETE", path = "api/appointments/{id}", hasBody = true)
    Call<Void> cancelAppointment(
        @Path("id") Long appointmentId,
        @Query("cancelledBy") String cancelledBy,
        @Body Object request
    );


    // ========== ADMIN ENDPOINTS ==========

    @GET("api/admin/dashboard")
    Call<DashboardResponse> getDashboard();

    @GET("api/users")
    Call<List<UserResponse>> getAllUsers();

    @GET("api/users/{id}")
    Call<UserResponse> getUserById(@Path("id") Long userId);

    @POST("api/users/patient")
    Call<UserResponse> createPatient(@Body CreateUserRequest request);

    @POST("api/users/doctor")
    Call<UserResponse> createDoctor(@Body CreateUserRequest request);

    @PUT("api/users/{id}")
    Call<UserResponse> updateUser(
        @Path("id") Long userId,
        @Body UpdateUserRequest request
    );

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") Long userId);
}
```

**ANNOTATIONS RETROFIT**:
- `@POST`, `@GET`, `@PUT`, `@DELETE`: Type de requête HTTP
- `@Body`: Corps de la requête (JSON)
- `@Path`: Paramètre dans l'URL (`/users/{id}`)
- `@Query`: Paramètre query string (`?patientId=1`)

**GÉNÉRATION AUTOMATIQUE**: Retrofit génère l'implémentation de cette interface

---

## 🛠️ UTILS (UTILITAIRES)

### **JwtInterceptor.java**

**RÔLE**: Intercepteur OkHttp qui ajoute automatiquement le token JWT dans TOUTES les requêtes

```java
public class JwtInterceptor implements Interceptor {
    private Context context;

    public JwtInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 1. Récupérer le token depuis SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        // 2. Si token existe, l'ajouter dans le header
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
            return chain.proceed(newRequest);
        }

        // 3. Sinon, continuer sans modification
        return chain.proceed(originalRequest);
    }
}
```

**OÙ UTILISÉ**: ApiClient.java → Ajouté comme intercepteur OkHttp

**FLOW**:
```
Activity appelle apiService.getPatientAppointments(patientId)
        ↓
JwtInterceptor intercepte la requête
        ↓
Ajoute header: "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
        ↓
Backend reçoit la requête avec le token
        ↓
JwtAuthenticationFilter vérifie le token
        ↓
Si valide → Traite la requête
Si invalide → Retourne 401 UNAUTHORIZED
```

---

### **ErrorParser.java**

**RÔLE**: Parser les erreurs retournées par le backend et les messages d'erreur réseau

```java
public class ErrorParser {

    /**
     * Parser le corps d'erreur du backend (ErrorResponse JSON)
     */
    public static String parseErrorMessage(ResponseBody errorBody) {
        if (errorBody == null) {
            return "Erreur inconnue";
        }

        try {
            String errorJson = errorBody.string();
            Gson gson = new Gson();
            ErrorResponse error = gson.fromJson(errorJson, ErrorResponse.class);
            return error.getMessage();
        } catch (Exception e) {
            return "Erreur serveur";
        }
    }

    /**
     * Obtenir un message d'erreur réseau lisible
     */
    public static String getNetworkErrorMessage(Throwable t) {
        if (t instanceof SocketTimeoutException) {
            return "Le serveur ne répond pas. Vérifiez votre connexion.";
        } else if (t instanceof IOException) {
            return "Erreur de connexion au serveur.";
        } else {
            return "Erreur: " + t.getMessage();
        }
    }
}
```

**UTILISÉ PAR**: TOUTES les Activities dans les callbacks `onResponse()` et `onFailure()`

**EXEMPLE**:
```java
@Override
public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
    if (response.isSuccessful()) {
        // Succès
    } else {
        // Erreur backend (400, 401, 409, etc.)
        String error = ErrorParser.parseErrorMessage(response.errorBody());
        showError(error);  // "Email ou mot de passe incorrect"
    }
}

@Override
public void onFailure(Call<LoginResponse> call, Throwable t) {
    // Erreur réseau
    String error = ErrorParser.getNetworkErrorMessage(t);
    showError(error);  // "Le serveur ne répond pas..."
}
```

---

## 👥 UI PATIENT

### **HomePatientActivity.java**

**RÔLE**: Dashboard du patient avec navigation vers les fonctionnalités principales

```java
public class HomePatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_patient);

        // 1. Afficher message de bienvenue
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String firstName = prefs.getString("user_first_name", "");
        welcomeMessage.setText("Bienvenue, " + firstName);

        // 2. Boutons navigation
        seeDoctorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorListActivity.class);
            startActivity(intent);
        });

        myAppointmentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAppointmentsActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Retour LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
```

**NAVIGATION**:
- Bouton "Voir les médecins" → `DoctorListActivity`
- Bouton "Mes rendez-vous" → `MyAppointmentsActivity`
- Bouton "Déconnexion" → `LoginActivity`

---

### **DoctorListActivity.java**

**RÔLE**: Afficher la liste de TOUS les médecins du cabinet pour que le patient choisisse

```java
public class DoctorListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Charger les médecins
        loadDoctors();
    }

    private void loadDoctors() {
        showLoading(true);

        apiService.getAllDoctors().enqueue(new Callback<List<DoctorResponse>>() {
            @Override
            public void onResponse(Call<List<DoctorResponse>> call, Response<List<DoctorResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<DoctorResponse> doctors = response.body();
                    adapter.updateDoctors(doctors);
                } else {
                    showError("Erreur lors du chargement");
                }
            }

            @Override
            public void onFailure(Call<List<DoctorResponse>> call, Throwable t) {
                showLoading(false);
                showError(ErrorParser.getNetworkErrorMessage(t));
            }
        });
    }
}
```

**API APPELÉE**: `GET /api/doctors` → `List<DoctorResponse>`

**ADAPTER UTILISÉ**: `DoctorAdapter` (affiche nom + spécialité, clic → AvailableTimeSlotsActivity)

---

### **AvailableTimeSlotsActivity.java**

**RÔLE**: Afficher un calendrier + les heures disponibles pour un médecin à une date choisie

```java
public class AvailableTimeSlotsActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TimeSlotAdapter adapter;

    private Long doctorId;
    private String doctorName;
    private String selectedDate;  // Format: "2026-02-03"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_timeslots);

        // Récupérer doctorId depuis Intent
        doctorId = getIntent().getLongExtra("doctorId", -1);
        doctorName = getIntent().getStringExtra("doctorName");

        // Setup CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadAvailableTimeSlots();
        });

        // Setup RecyclerView (grille 3 colonnes)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new TimeSlotAdapter(new ArrayList<>(), this::onTimeSlotClick);
        recyclerView.setAdapter(adapter);
    }

    private void loadAvailableTimeSlots() {
        showLoading(true);

        apiService.getAvailableTimeSlots(doctorId, selectedDate)
            .enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        List<String> timeSlots = response.body();  // ["09:00:00", "09:30:00", ...]
                        adapter.updateTimeSlots(timeSlots);
                    }
                }
            });
    }

    private void onTimeSlotClick(String timeSlot) {
        // Patient clique sur une heure → BookAppointmentActivity
        Intent intent = new Intent(this, BookAppointmentActivity.class);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("dateTime", selectedDate + "T" + timeSlot);  // "2026-02-03T09:00:00"
        startActivity(intent);
    }
}
```

**API APPELÉE**: `GET /api/timeslots/available?doctorId={id}&date={date}` → `List<String>`

**ADAPTER UTILISÉ**: `ui/patient/TimeSlotAdapter` (grille de boutons avec heures)

---

### **BookAppointmentActivity.java**

**RÔLE**: Confirmer le RDV choisi et saisir le motif de consultation

```java
public class BookAppointmentActivity extends AppCompatActivity {

    private Long doctorId;
    private String doctorName;
    private String dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        // Récupérer données depuis Intent
        doctorId = getIntent().getLongExtra("doctorId", -1);
        doctorName = getIntent().getStringExtra("doctorName");
        dateTime = getIntent().getStringExtra("dateTime");  // "2026-02-03T09:00:00"

        // Afficher résumé
        doctorNameText.setText(doctorName);
        dateTimeText.setText(formatDateTime(dateTime));  // "03/02/2026 à 09:00"

        // Bouton confirmer
        confirmButton.setOnClickListener(v -> confirmAppointment());
    }

    private void confirmAppointment() {
        String reason = reasonInput.getText().toString().trim();

        if (reason.isEmpty()) {
            showError("Le motif est obligatoire");
            return;
        }

        // Récupérer patientId depuis SharedPreferences
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        Long patientId = prefs.getLong("patient_id", -1);

        // Créer requête
        CreateAppointmentRequest request = new CreateAppointmentRequest(doctorId, dateTime, reason);

        // Appel API
        showLoading(true);
        apiService.createAppointment(patientId, request)
            .enqueue(new Callback<AppointmentResponse>() {
                @Override
                public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(BookAppointmentActivity.this,
                            "Rendez-vous créé avec succès", Toast.LENGTH_SHORT).show();

                        // Rediriger vers MyAppointmentsActivity
                        Intent intent = new Intent(BookAppointmentActivity.this, MyAppointmentsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else if (response.code() == 409) {
                        showError("Ce créneau n'est plus disponible");
                    } else {
                        showError(ErrorParser.parseErrorMessage(response.errorBody()));
                    }
                }
            });
    }
}
```

**API APPELÉE**: `POST /api/appointments?patientId={id}` avec `CreateAppointmentRequest`

**NAVIGATION**: Succès → `MyAppointmentsActivity`

---

### **MyAppointmentsActivity.java**

**RÔLE**: Afficher l'historique complet des RDV du patient (futurs, passés, annulés)

```java
public class MyAppointmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();  // Rafraîchir à chaque retour sur cet écran
    }

    private void loadAppointments() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        Long patientId = prefs.getLong("patient_id", -1);

        showLoading(true);
        apiService.getPatientAppointments(patientId)
            .enqueue(new Callback<List<AppointmentResponse>>() {
                @Override
                public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        List<AppointmentResponse> appointments = response.body();

                        if (appointments.isEmpty()) {
                            showEmptyMessage();
                        } else {
                            adapter.updateAppointments(appointments);
                        }
                    }
                }
            });
    }
}
```

**API APPELÉE**: `GET /api/appointments/patient/{patientId}` → `List<AppointmentResponse>`

**ADAPTER UTILISÉ**: `AppointmentAdapter` (affiche RDV avec médecin, date, statut)

---

## 👨‍⚕️ UI DOCTOR

### **HomeDoctorActivity.java**

**RÔLE**: Dashboard du médecin avec navigation vers ses fonctionnalités

```java
public class HomeDoctorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_doctor);

        // Message de bienvenue
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String firstName = prefs.getString("user_first_name", "");
        welcomeMessage.setText("Bienvenue, Dr. " + firstName);

        // Navigation
        myAppointmentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAppointmentsActivity.class);
            startActivity(intent);
        });

        manageTimeSlotsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorTimeSlotsActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());
    }
}
```

**NAVIGATION**:
- "Mes rendez-vous" → `DoctorAppointmentsActivity`
- "Gérer mes créneaux" → `DoctorTimeSlotsActivity`
- "Déconnexion" → `LoginActivity`

---

### **DoctorAppointmentsActivity.java**

**RÔLE**: Afficher tous les RDV du médecin avec possibilité de confirmer ou annuler

```java
public class DoctorAppointmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorAppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointments);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAppointmentAdapter(
            new ArrayList<>(),
            this::onConfirmAppointment,
            this::onCancelAppointment
        );
        recyclerView.setAdapter(adapter);

        loadAppointments();
    }

    private void loadAppointments() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        Long doctorId = prefs.getLong("doctor_id", -1);

        apiService.getDoctorAppointments(doctorId)
            .enqueue(new Callback<List<AppointmentResponse>>() {
                @Override
                public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateAppointments(response.body());
                    }
                }
            });
    }

    private void onConfirmAppointment(AppointmentResponse appointment) {
        apiService.confirmAppointment(appointment.getId())
            .enqueue(new Callback<AppointmentResponse>() {
                @Override
                public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DoctorAppointmentsActivity.this,
                            "Rendez-vous confirmé", Toast.LENGTH_SHORT).show();
                        loadAppointments();  // Rafraîchir
                    }
                }
            });
    }

    private void onCancelAppointment(AppointmentResponse appointment) {
        // Dialog pour saisir la raison d'annulation
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText reasonInput = new EditText(this);

        builder.setTitle("Annuler le rendez-vous")
            .setMessage("Raison de l'annulation:")
            .setView(reasonInput)
            .setPositiveButton("Annuler RDV", (dialog, which) -> {
                String reason = reasonInput.getText().toString();

                Map<String, String> body = new HashMap<>();
                body.put("reason", reason);

                apiService.cancelAppointment(appointment.getId(), "DOCTOR", body)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(DoctorAppointmentsActivity.this,
                                    "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                                loadAppointments();
                            }
                        }
                    });
            })
            .setNegativeButton("Retour", null)
            .show();
    }
}
```

**API APPELÉE**:
- `GET /api/appointments/doctor/{doctorId}` → Liste RDV
- `POST /api/appointments/{id}/confirm` → Confirmer RDV
- `DELETE /api/appointments/{id}?cancelledBy=DOCTOR` → Annuler RDV

---

### **DoctorTimeSlotsActivity.java**

**RÔLE**: Afficher tous les créneaux du médecin avec possibilité d'ajouter/modifier/supprimer

```java
public class DoctorTimeSlotsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorTimeSlotAdapter adapter;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_timeslots);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorTimeSlotAdapter(
            new ArrayList<>(),
            new DoctorTimeSlotAdapter.OnTimeSlotActionListener() {
                @Override
                public void onEdit(TimeSlotResponse timeSlot) {
                    // Clic → Modifier
                    Intent intent = new Intent(DoctorTimeSlotsActivity.this, DoctorEditTimeSlotActivity.class);
                    intent.putExtra("timeSlotId", timeSlot.getId());
                    intent.putExtra("dayOfWeek", timeSlot.getDayOfWeek());
                    intent.putExtra("startTime", timeSlot.getStartTime());
                    intent.putExtra("endTime", timeSlot.getEndTime());
                    startActivity(intent);
                }

                @Override
                public void onDelete(TimeSlotResponse timeSlot) {
                    // Long clic → Supprimer avec confirmation
                    showDeleteConfirmDialog(timeSlot);
                }
            }
        );
        recyclerView.setAdapter(adapter);

        // FAB pour ajouter
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorAddTimeSlotActivity.class);
            startActivity(intent);
        });

        loadTimeSlots();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTimeSlots();  // Rafraîchir après ajout/modification
    }

    private void loadTimeSlots() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        Long doctorId = prefs.getLong("doctor_id", -1);

        apiService.getDoctorTimeSlots(doctorId)
            .enqueue(new Callback<List<TimeSlotResponse>>() {
                @Override
                public void onResponse(Call<List<TimeSlotResponse>> call, Response<List<TimeSlotResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<TimeSlotResponse> timeSlots = response.body();

                        // Trier par jour de semaine (Lundi=1, Dimanche=7)
                        Collections.sort(timeSlots, (a, b) -> {
                            Map<String, Integer> dayOrder = new HashMap<>();
                            dayOrder.put("MONDAY", 1);
                            dayOrder.put("TUESDAY", 2);
                            // ...
                            return dayOrder.get(a.getDayOfWeek()) - dayOrder.get(b.getDayOfWeek());
                        });

                        adapter.updateTimeSlots(timeSlots);
                    }
                }
            });
    }

    private void showDeleteConfirmDialog(TimeSlotResponse timeSlot) {
        new AlertDialog.Builder(this)
            .setTitle("Supprimer le créneau")
            .setMessage("Voulez-vous vraiment supprimer ce créneau?")
            .setPositiveButton("Supprimer", (dialog, which) -> {
                apiService.deleteTimeSlot(timeSlot.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(DoctorTimeSlotsActivity.this,
                                    "Créneau supprimé", Toast.LENGTH_SHORT).show();
                                loadTimeSlots();
                            }
                        }
                    });
            })
            .setNegativeButton("Annuler", null)
            .show();
    }
}
```

**API APPELÉE**:
- `GET /api/timeslots/doctor/{doctorId}` → Liste créneaux
- `DELETE /api/timeslots/{id}` → Supprimer créneau

---

### **DoctorAddTimeSlotActivity.java**

**RÔLE**: Formulaire pour créer un nouveau créneau horaire

```java
public class DoctorAddTimeSlotActivity extends AppCompatActivity {

    private Spinner daySpinner;
    private TextInputEditText startTimeInput;
    private TextInputEditText endTimeInput;

    private String selectedStartTime;  // "09:00:00"
    private String selectedEndTime;    // "12:00:00"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_add_timeslot);

        // Setup Spinner (jours de la semaine)
        String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        daySpinner.setAdapter(adapter);

        // TimePickerDialog pour heure début
        startTimeInput.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedStartTime = String.format("%02d:%02d:00", hourOfDay, minute);
                    startTimeInput.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                9, 0, true);  // Default: 09:00
            picker.show();
        });

        // TimePickerDialog pour heure fin
        endTimeInput.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedEndTime = String.format("%02d:%02d:00", hourOfDay, minute);
                    endTimeInput.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                12, 0, true);  // Default: 12:00
            picker.show();
        });

        // Bouton créer
        createButton.setOnClickListener(v -> createTimeSlot());
    }

    private void createTimeSlot() {
        // Validation
        if (selectedStartTime == null || selectedEndTime == null) {
            showError("Sélectionnez les heures");
            return;
        }

        // Vérifier que endTime > startTime
        if (selectedEndTime.compareTo(selectedStartTime) <= 0) {
            showError("L'heure de fin doit être après l'heure de début");
            return;
        }

        // Mapper jour FR → EN
        Map<String, String> dayMap = new HashMap<>();
        dayMap.put("Lundi", "MONDAY");
        dayMap.put("Mardi", "TUESDAY");
        // ...

        String dayFr = daySpinner.getSelectedItem().toString();
        String dayEn = dayMap.get(dayFr);

        // Récupérer doctorId
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        Long doctorId = prefs.getLong("doctor_id", -1);

        // Créer requête
        CreateTimeSlotRequest request = new CreateTimeSlotRequest(
            doctorId, dayEn, selectedStartTime, selectedEndTime
        );

        // Appel API
        apiService.createTimeSlot(request)
            .enqueue(new Callback<TimeSlotResponse>() {
                @Override
                public void onResponse(Call<TimeSlotResponse> call, Response<TimeSlotResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DoctorAddTimeSlotActivity.this,
                            "Créneau créé avec succès", Toast.LENGTH_SHORT).show();
                        finish();  // Retour DoctorTimeSlotsActivity
                    } else if (response.code() == 409) {
                        showError("Ce créneau chevauche un créneau existant");
                    } else {
                        showError(ErrorParser.parseErrorMessage(response.errorBody()));
                    }
                }
            });
    }
}
```

**API APPELÉE**: `POST /api/timeslots` avec `CreateTimeSlotRequest`

**VALIDATION**:
- Client: `endTime > startTime`
- Backend: Pas de chevauchement avec créneaux existants (erreur 409)

---

### **DoctorEditTimeSlotActivity.java**

**RÔLE**: Formulaire pré-rempli pour modifier ou supprimer un créneau existant

```java
public class DoctorEditTimeSlotActivity extends AppCompatActivity {

    private Long timeSlotId;
    private Spinner daySpinner;
    private TextInputEditText startTimeInput;
    private TextInputEditText endTimeInput;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_edit_timeslot);

        // Récupérer données depuis Intent
        timeSlotId = getIntent().getLongExtra("timeSlotId", -1);
        String dayOfWeek = getIntent().getStringExtra("dayOfWeek");  // "MONDAY"
        String startTime = getIntent().getStringExtra("startTime");  // "09:00:00"
        String endTime = getIntent().getStringExtra("endTime");      // "12:00:00"

        // Pré-remplir le formulaire
        prefillForm(dayOfWeek, startTime, endTime);

        // Bouton enregistrer
        saveButton.setOnClickListener(v -> updateTimeSlot());

        // Bouton supprimer
        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void updateTimeSlot() {
        // Récupérer nouvelles valeurs
        String dayEn = getDayInEnglish(daySpinner.getSelectedItem().toString());

        UpdateTimeSlotRequest request = new UpdateTimeSlotRequest(
            dayEn, selectedStartTime, selectedEndTime
        );

        apiService.updateTimeSlot(timeSlotId, request)
            .enqueue(new Callback<TimeSlotResponse>() {
                @Override
                public void onResponse(Call<TimeSlotResponse> call, Response<TimeSlotResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DoctorEditTimeSlotActivity.this,
                            "Créneau modifié avec succès", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (response.code() == 409) {
                        showError("Ce créneau chevauche un créneau existant");
                    }
                }
            });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Supprimer le créneau")
            .setMessage("Voulez-vous vraiment supprimer ce créneau?")
            .setPositiveButton("Supprimer", (dialog, which) -> deleteTimeSlot())
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void deleteTimeSlot() {
        apiService.deleteTimeSlot(timeSlotId)
            .enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DoctorEditTimeSlotActivity.this,
                            "Créneau supprimé", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
    }
}
```

**API APPELÉE**:
- `PUT /api/timeslots/{id}` → Modifier
- `DELETE /api/timeslots/{id}` → Supprimer

---

## 👨‍💼 UI ADMIN

### **DashboardAdminActivity.java**

**RÔLE**: Tableau de bord admin avec statistiques et navigation vers gestion

```java
public class DashboardAdminActivity extends AppCompatActivity {

    private TextView appointmentsTodayText;
    private TextView appointmentsWeekText;
    private TextView pendingCountText;
    private TextView confirmedCountText;
    private TextView totalDoctorsText;
    private TextView totalPatientsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        // Charger statistiques
        loadDashboard();

        // Boutons navigation
        manageDoctorsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDoctorsListActivity.class);
            startActivity(intent);
        });

        managePatientsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPatientsListActivity.class);
            startActivity(intent);
        });

        viewAllAppointmentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAllAppointmentsActivity.class);
            startActivity(intent);
        });
    }

    private void loadDashboard() {
        apiService.getDashboard().enqueue(new Callback<DashboardResponse>() {
            @Override
            public void onResponse(Call<DashboardResponse> call, Response<DashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardResponse data = response.body();

                    // Afficher statistiques
                    appointmentsTodayText.setText(String.valueOf(data.getTotalAppointmentsToday()));
                    appointmentsWeekText.setText(String.valueOf(data.getTotalAppointmentsWeek()));
                    pendingCountText.setText(String.valueOf(data.getPendingCount()));
                    confirmedCountText.setText(String.valueOf(data.getConfirmedCount()));
                    totalDoctorsText.setText(String.valueOf(data.getTotalDoctors()));
                    totalPatientsText.setText(String.valueOf(data.getTotalPatients()));
                }
            }
        });
    }
}
```

**API APPELÉE**: `GET /api/admin/dashboard` → `DashboardResponse`

**NAVIGATION**:
- "Gérer les médecins" → `AdminDoctorsListActivity`
- "Gérer les patients" → `AdminPatientsListActivity`
- "Voir tous les RDV" → `AdminAllAppointmentsActivity`

---

### **AdminDoctorsListActivity.java**

**RÔLE**: Liste de TOUS les médecins du cabinet avec accès CRUD

```java
public class AdminDoctorsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminDoctorAdapter adapter;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctors_list);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminDoctorAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // FAB pour ajouter médecin
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAddDoctorActivity.class);
            startActivity(intent);
        });

        loadDoctors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctors();  // Rafraîchir après ajout/modification
    }

    private void loadDoctors() {
        apiService.getAllDoctors().enqueue(new Callback<List<DoctorResponse>>() {
            @Override
            public void onResponse(Call<List<DoctorResponse>> call, Response<List<DoctorResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateDoctors(response.body());
                }
            }
        });
    }
}
```

**API APPELÉE**: `GET /api/doctors` → `List<DoctorResponse>`

**ADAPTER**: `AdminDoctorAdapter` (clic item → `AdminDoctorDetailsActivity`)

---

### **AdminDoctorDetailsActivity.java**

**RÔLE**: Afficher les détails d'un médecin avec possibilité de modifier ou supprimer

```java
public class AdminDoctorDetailsActivity extends AppCompatActivity {

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctor_details);

        userId = getIntent().getLongExtra("userId", -1);

        // Charger détails
        loadDoctorDetails();

        // Bouton modifier
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEditDoctorActivity.class);
            intent.putExtra("userId", userId);
            // Passer aussi les données actuelles pour pré-remplir
            intent.putExtra("email", doctorEmail.getText().toString());
            intent.putExtra("firstName", doctorFirstName.getText().toString());
            intent.putExtra("lastName", doctorLastName.getText().toString());
            intent.putExtra("phone", doctorPhone.getText().toString());
            intent.putExtra("specialty", doctorSpecialty.getText().toString());
            startActivity(intent);
        });

        // Bouton supprimer
        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void loadDoctorDetails() {
        apiService.getUserById(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse doctor = response.body();

                    // Afficher détails
                    doctorName.setText(doctor.getFirstName() + " " + doctor.getLastName());
                    doctorEmail.setText(doctor.getEmail());
                    doctorPhone.setText(doctor.getPhone());
                    doctorSpecialty.setText(doctor.getSpecialty());
                }
            }
        });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Supprimer le médecin")
            .setMessage("Voulez-vous vraiment supprimer ce médecin?")
            .setPositiveButton("Supprimer", (dialog, which) -> deleteDoctor())
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void deleteDoctor() {
        apiService.deleteUser(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminDoctorDetailsActivity.this,
                        "Médecin supprimé", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
```

**API APPELÉE**:
- `GET /api/users/{id}` → Détails
- `DELETE /api/users/{id}` → Supprimer

---

### **AdminAddDoctorActivity.java**

**RÔLE**: Formulaire pour créer un nouveau médecin

```java
public class AdminAddDoctorActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText specialtyInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_doctor);

        saveButton.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        // Récupérer valeurs
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String specialty = specialtyInput.getText().toString().trim();

        // Validation
        if (email.isEmpty() || password.length() < 6 || firstName.isEmpty() ||
            lastName.isEmpty() || specialty.isEmpty()) {
            showError("Tous les champs sont obligatoires");
            return;
        }

        // Créer requête
        CreateUserRequest request = new CreateUserRequest(
            email, password, firstName, lastName, phone, specialty
        );

        // Appel API
        apiService.createDoctor(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminAddDoctorActivity.this,
                        "Médecin créé avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 409) {
                    showError("Email déjà utilisé");
                } else {
                    showError(ErrorParser.parseErrorMessage(response.errorBody()));
                }
            }
        });
    }
}
```

**API APPELÉE**: `POST /api/users/doctor` avec `CreateUserRequest`

**VALIDATION**: Email unique, password 6+, specialty obligatoire

---

### **AdminEditDoctorActivity.java**

**RÔLE**: Formulaire pré-rempli pour modifier un médecin

```java
public class AdminEditDoctorActivity extends AppCompatActivity {

    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_doctor);

        // Récupérer données depuis Intent
        userId = getIntent().getLongExtra("userId", -1);
        String email = getIntent().getStringExtra("email");
        String firstName = getIntent().getStringExtra("firstName");
        String lastName = getIntent().getStringExtra("lastName");
        String phone = getIntent().getStringExtra("phone");
        String specialty = getIntent().getStringExtra("specialty");

        // Pré-remplir
        emailInput.setText(email);
        firstNameInput.setText(firstName);
        lastNameInput.setText(lastName);
        phoneInput.setText(phone);
        specialtyInput.setText(specialty);

        saveButton.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();  // Optionnel
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String specialty = specialtyInput.getText().toString().trim();

        // Validation
        if (email.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || specialty.isEmpty()) {
            showError("Les champs obligatoires ne peuvent pas être vides");
            return;
        }

        // Vérifier password seulement s'il est fourni
        if (!password.isEmpty() && password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        // Créer requête (password vide = ne pas modifier)
        UpdateUserRequest request = new UpdateUserRequest(
            email,
            password.isEmpty() ? "" : password,
            firstName,
            lastName,
            phone,
            specialty
        );

        apiService.updateUser(userId, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminEditDoctorActivity.this,
                        "Médecin modifié avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    showError(ErrorParser.parseErrorMessage(response.errorBody()));
                }
            }
        });
    }
}
```

**API APPELÉE**: `PUT /api/users/{id}` avec `UpdateUserRequest`

**NOTE**: Password vide = pas de modification du mot de passe

---

### Gestion Patients (Pattern identique)

Les activities pour les patients suivent le même pattern:

- **AdminPatientsListActivity** → `GET /api/users` (filtre PATIENT)
- **AdminPatientDetailsActivity** → `GET /api/users/{id}`
- **AdminAddPatientActivity** → `POST /api/users/patient` (sans specialty)
- **AdminEditPatientActivity** → `PUT /api/users/{id}`

---

### **AdminAllAppointmentsActivity.java**

**RÔLE**: Monitoring de TOUS les RDV (tous médecins, tous patients)

```java
public class AdminAllAppointmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorAppointmentAdapter adapter;  // Réutilise adapter médecin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_all_appointments);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DoctorAppointmentAdapter(
            new ArrayList<>(),
            this::onConfirmAppointment,
            this::onCancelAppointment
        );
        recyclerView.setAdapter(adapter);

        loadAllAppointments();
    }

    private void loadAllAppointments() {
        apiService.getAllAppointments().enqueue(new Callback<List<AppointmentResponse>>() {
            @Override
            public void onResponse(Call<List<AppointmentResponse>> call, Response<List<AppointmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateAppointments(response.body());
                }
            }
        });
    }

    private void onConfirmAppointment(AppointmentResponse appointment) {
        apiService.confirmAppointment(appointment.getId())
            .enqueue(new Callback<AppointmentResponse>() {
                @Override
                public void onResponse(Call<AppointmentResponse> call, Response<AppointmentResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminAllAppointmentsActivity.this,
                            "Rendez-vous confirmé", Toast.LENGTH_SHORT).show();
                        loadAllAppointments();
                    }
                }
            });
    }

    private void onCancelAppointment(AppointmentResponse appointment) {
        // Dialog pour raison
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText reasonInput = new EditText(this);

        builder.setTitle("Annuler le rendez-vous")
            .setView(reasonInput)
            .setPositiveButton("Annuler RDV", (dialog, which) -> {
                Map<String, String> body = new HashMap<>();
                body.put("reason", reasonInput.getText().toString());

                apiService.cancelAppointment(appointment.getId(), "ADMIN", body)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminAllAppointmentsActivity.this,
                                    "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
                                loadAllAppointments();
                            }
                        }
                    });
            })
            .setNegativeButton("Retour", null)
            .show();
    }
}
```

**API APPELÉE**: `GET /api/appointments` → TOUS les RDV (admin uniquement)

---

## 🔄 ADAPTERS (RecyclerView)

### **AppointmentAdapter.java** (Patient)

**RÔLE**: Afficher la liste des RDV du patient dans MyAppointmentsActivity

```java
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<AppointmentResponse> appointments;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppointmentResponse appointment = appointments.get(position);

        // Afficher docteur
        holder.doctorName.setText(appointment.getDoctorName());
        holder.doctorSpecialty.setText(appointment.getSpecialtyOrDefault());

        // Afficher date/heure formatée
        holder.dateTime.setText(appointment.getFormattedDateTime());  // "03/02/2026 à 14:00"

        // Afficher motif
        holder.reason.setText(appointment.getReason());

        // Afficher statut avec couleur
        String status = appointment.getStatusLabel();  // "En attente", "Confirmé", "Annulé"
        holder.status.setText(status);

        // Couleur selon statut
        if ("CONFIRMED".equals(appointment.getStatus())) {
            holder.status.setTextColor(Color.GREEN);
        } else if ("CANCELLED".equals(appointment.getStatus())) {
            holder.status.setTextColor(Color.RED);
        } else {
            holder.status.setTextColor(Color.ORANGE);
        }
    }

    public void updateAppointments(List<AppointmentResponse> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName;
        TextView doctorSpecialty;
        TextView dateTime;
        TextView reason;
        TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.tv_doctor_name);
            doctorSpecialty = itemView.findViewById(R.id.tv_doctor_specialty);
            dateTime = itemView.findViewById(R.id.tv_date_time);
            reason = itemView.findViewById(R.id.tv_reason);
            status = itemView.findViewById(R.id.tv_status);
        }
    }
}
```

**LAYOUT**: `item_appointment.xml` (CardView avec TextViews)

---

### **DoctorAppointmentAdapter.java** (Doctor + Admin)

**RÔLE**: Afficher RDV avec boutons Confirmer/Annuler

```java
public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.ViewHolder> {

    private List<AppointmentResponse> appointments;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onConfirm(AppointmentResponse appointment);
        void onCancel(AppointmentResponse appointment);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppointmentResponse appointment = appointments.get(position);

        // Afficher patient
        holder.patientName.setText(appointment.getPatientName());
        holder.patientPhone.setText(appointment.getPatientPhone());

        // Afficher date/heure
        holder.dateTime.setText(appointment.getFormattedDateTime());

        // Afficher motif
        holder.reason.setText(appointment.getReason());

        // Afficher statut
        holder.status.setText(appointment.getStatusLabel());

        // Boutons selon statut
        if ("PENDING".equals(appointment.getStatus())) {
            // Afficher boutons Confirmer + Annuler
            holder.confirmButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.VISIBLE);

            holder.confirmButton.setOnClickListener(v -> listener.onConfirm(appointment));
            holder.cancelButton.setOnClickListener(v -> listener.onCancel(appointment));
        } else {
            // Cacher boutons si déjà confirmé ou annulé
            holder.confirmButton.setVisibility(View.GONE);
            holder.cancelButton.setVisibility(View.GONE);
        }
    }
}
```

**UTILISÉ PAR**:
- DoctorAppointmentsActivity
- AdminAllAppointmentsActivity

---

### **DoctorAdapter.java** (Patient)

**RÔLE**: Afficher liste médecins pour que le patient choisisse

```java
public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private List<DoctorResponse> doctors;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DoctorResponse doctor = doctors.get(position);

        // Afficher nom complet
        holder.doctorName.setText(doctor.getFullName());  // "Dr. Pierre Martin"

        // Afficher spécialité
        holder.doctorSpecialty.setText(doctor.getSpecialtyOrDefault());  // "Cardiologue"

        // Clic → AvailableTimeSlotsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AvailableTimeSlotsActivity.class);
            intent.putExtra("doctorId", doctor.getId());
            intent.putExtra("doctorName", doctor.getFullName());
            v.getContext().startActivity(intent);
        });
    }
}
```

**UTILISÉ PAR**: DoctorListActivity

---

### **AdminDoctorAdapter.java** (Admin)

**RÔLE**: Afficher liste médecins pour l'admin (accès détails)

```java
public class AdminDoctorAdapter extends RecyclerView.Adapter<AdminDoctorAdapter.ViewHolder> {

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DoctorResponse doctor = doctors.get(position);

        holder.doctorName.setText(doctor.getFullName());
        holder.doctorSpecialty.setText(doctor.getSpecialtyOrDefault());

        // Clic → AdminDoctorDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AdminDoctorDetailsActivity.class);
            intent.putExtra("userId", doctor.getUserId());
            intent.putExtra("specialty", doctor.getSpecialty());
            v.getContext().startActivity(intent);
        });
    }
}
```

**UTILISÉ PAR**: AdminDoctorsListActivity

---

### **DoctorTimeSlotAdapter.java** (Doctor)

**RÔLE**: Afficher créneaux du médecin avec actions Modifier/Supprimer

```java
public class DoctorTimeSlotAdapter extends RecyclerView.Adapter<DoctorTimeSlotAdapter.ViewHolder> {

    private List<TimeSlotResponse> timeSlots;
    private OnTimeSlotActionListener listener;

    public interface OnTimeSlotActionListener {
        void onEdit(TimeSlotResponse timeSlot);
        void onDelete(TimeSlotResponse timeSlot);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TimeSlotResponse timeSlot = timeSlots.get(position);

        // Afficher jour en français
        holder.dayOfWeek.setText(timeSlot.getDayOfWeekFr());  // "Lundi"

        // Afficher horaires
        holder.timeRange.setText(timeSlot.getTimeRange());  // "09:00 - 12:00"

        // Afficher durée
        holder.duration.setText(timeSlot.getFormattedDuration());  // "3h"

        // Clic → Modifier
        holder.itemView.setOnClickListener(v -> listener.onEdit(timeSlot));

        // Long clic → Supprimer
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(timeSlot);
            return true;
        });
    }
}
```

**UTILISÉ PAR**: DoctorTimeSlotsActivity

---

### **ui/patient/TimeSlotAdapter.java** (Patient)

**RÔLE**: Grille de boutons avec heures disponibles

```java
public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private List<String> timeSlots;  // ["09:00:00", "09:30:00", ...]
    private OnTimeSlotClickListener listener;

    @Override
    public void onBindViewHolder(TimeSlotViewHolder holder, int position) {
        String timeSlot = timeSlots.get(position);

        // Formater heure (enlever secondes)
        String displayTime = timeSlot.substring(0, 5);  // "09:00:00" → "09:00"
        holder.timeText.setText(displayTime);

        // Clic → BookAppointmentActivity
        holder.itemView.setOnClickListener(v -> listener.onTimeSlotClick(timeSlot));
    }
}
```

**LAYOUT**: `item_time_slot.xml` (Button Material)

**UTILISÉ PAR**: AvailableTimeSlotsActivity (GridLayoutManager 3 colonnes)

---

## 🔄 FLOWS COMPLETS

### Flow 1: Patient Réserve un Rendez-Vous

```
1. HomePatientActivity
   Clic "Voir les médecins"
   ↓
2. DoctorListActivity
   - API: GET /api/doctors
   - Adapter: DoctorAdapter
   - Affiche tous les médecins
   Clic sur Dr. Martin (Cardiologue)
   ↓
3. AvailableTimeSlotsActivity
   - Affiche CalendarView
   Patient sélectionne date: 03/02/2026
   - API: GET /api/timeslots/available?doctorId=1&date=2026-02-03
   - Retour: ["09:00:00", "09:30:00", "10:00:00", ...]
   - Adapter: TimeSlotAdapter (grille 3 colonnes)
   Patient clique sur "09:00"
   ↓
4. BookAppointmentActivity
   - Affiche résumé:
     * Médecin: Dr. Martin
     * Date: 03/02/2026 à 09:00
   - Patient saisit motif: "Consultation cardiaque"
   Clic "Confirmer"
   - API: POST /api/appointments?patientId=1
     Body: {
       "doctorId": 1,
       "dateTime": "2026-02-03T09:00:00",
       "reason": "Consultation cardiaque"
     }
   - Backend vérifie créneau disponible
   - Si OK (201 CREATED) → Retour AppointmentResponse
   - Si créneau pris (409 CONFLICT) → Erreur affichée
   ↓
5. MyAppointmentsActivity
   - API: GET /api/appointments/patient/1
   - Adapter: AppointmentAdapter
   - Affiche le nouveau RDV (status: PENDING)
```

---

### Flow 2: Médecin Gère ses Créneaux

```
1. HomeDoctorActivity
   Clic "Gérer mes créneaux"
   ↓
2. DoctorTimeSlotsActivity
   - API: GET /api/timeslots/doctor/1
   - Retour: [
       {id: 1, dayOfWeek: "MONDAY", dayOfWeekFr: "Lundi", startTime: "09:00:00", endTime: "12:00:00"},
       {id: 2, dayOfWeek: "TUESDAY", dayOfWeekFr: "Mardi", startTime: "14:00:00", endTime: "18:00:00"}
     ]
   - Adapter: DoctorTimeSlotAdapter
   - Affiche: Lundi 09:00 - 12:00 (3h)
               Mardi 14:00 - 18:00 (4h)

   === Option A: Ajouter ===
   Clic FAB "+"
   ↓
3. DoctorAddTimeSlotActivity
   - Spinner: Sélectionne "Mercredi"
   - TimePickerDialog: Sélectionne 09:00
   - TimePickerDialog: Sélectionne 12:00
   Clic "Créer"
   - Validation: endTime (12:00) > startTime (09:00) ✓
   - API: POST /api/timeslots
     Body: {
       "doctorId": 1,
       "dayOfWeek": "WEDNESDAY",
       "startTime": "09:00:00",
       "endTime": "12:00:00"
     }
   - Backend vérifie chevauchement
   - Si OK (201 CREATED) → finish() → Retour DoctorTimeSlotsActivity
   - Si chevauchement (409 CONFLICT) → Erreur affichée

   === Option B: Modifier ===
   Clic sur créneau "Lundi 09:00 - 12:00"
   ↓
3. DoctorEditTimeSlotActivity
   - Formulaire pré-rempli:
     * Jour: Lundi
     * Heure début: 09:00
     * Heure fin: 12:00
   - Médecin change: Heure fin → 13:00
   Clic "Enregistrer"
   - API: PUT /api/timeslots/1
     Body: {
       "dayOfWeek": "MONDAY",
       "startTime": "09:00:00",
       "endTime": "13:00:00"
     }
   - Si OK (200 OK) → finish() → Retour DoctorTimeSlotsActivity

   === Option C: Supprimer ===
   Long clic sur créneau
   - Dialog confirmation
   Clic "Supprimer"
   - API: DELETE /api/timeslots/1
   - Si OK (204 NO CONTENT) → Créneau supprimé de la liste
```

---

### Flow 3: Médecin Confirme un RDV

```
1. HomeDoctorActivity
   Clic "Mes rendez-vous"
   ↓
2. DoctorAppointmentsActivity
   - API: GET /api/appointments/doctor/1
   - Retour: [
       {
         id: 5,
         patientName: "Jean Dupont",
         patientPhone: "0612345678",
         dateTime: "2026-02-03T09:00:00",
         reason: "Consultation cardiaque",
         status: "PENDING"
       }
     ]
   - Adapter: DoctorAppointmentAdapter
   - Affiche: Jean Dupont
              03/02/2026 à 09:00
              Consultation cardiaque
              [Bouton Confirmer] [Bouton Annuler]

   Clic "Confirmer"
   - API: POST /api/appointments/5/confirm
   - Backend: status PENDING → CONFIRMED
   - Retour (200 OK): {id: 5, status: "CONFIRMED", ...}
   - Rafraîchir liste → Boutons cachés, statut "Confirmé" en vert
```

---

### Flow 4: Admin Crée un Médecin

```
1. DashboardAdminActivity
   Clic "Gérer les médecins"
   ↓
2. AdminDoctorsListActivity
   - API: GET /api/doctors
   - Adapter: AdminDoctorAdapter
   Clic FAB "+"
   ↓
3. AdminAddDoctorActivity
   - Formulaire vide
   Admin saisit:
   * Email: pierre.martin@email.com
   * Password: Password123
   * Prénom: Pierre
   * Nom: Martin
   * Téléphone: 0612345678
   * Spécialité: Cardiologue
   Clic "Créer"
   - Validation locale: Tous les champs remplis ✓
   - API: POST /api/users/doctor
     Body: {
       "email": "pierre.martin@email.com",
       "password": "Password123",
       "firstName": "Pierre",
       "lastName": "Martin",
       "phone": "0612345678",
       "specialty": "Cardiologue"
     }
   - Backend:
     1. Hash password (BCrypt)
     2. Créer User (role=DOCTOR)
     3. Créer Doctor (lié à User)
   - Si OK (201 CREATED) → Toast "Médecin créé" → finish()
   - Si email existe (409 CONFLICT) → Erreur "Email déjà utilisé"
   ↓
4. AdminDoctorsListActivity (onResume)
   - Rafraîchir liste
   - Nouveau médecin apparaît: Dr. Pierre Martin (Cardiologue)
```

---

## 🔧 PATTERN TECHNIQUE COMMUN

### Structure d'une Activity Type

```java
public class ExampleActivity extends AppCompatActivity {

    // 1. Déclaration des vues
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView errorMessage;

    // 2. API Service
    private ApiService apiService;

    // 3. onCreate()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        // 3.1. Initialiser Retrofit
        apiService = ApiClient.getClient(this).create(ApiService.class);

        // 3.2. Lier les vues
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        errorMessage = findViewById(R.id.error_message);

        // 3.3. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExampleAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 3.4. Charger données
        loadData();
    }

    // 4. onResume() - Rafraîchir si besoin
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // 5. Appel API
    private void loadData() {
        showLoading(true);

        apiService.getData().enqueue(new Callback<DataResponse>() {
            @Override
            public void onResponse(Call<DataResponse> call, Response<DataResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Succès
                    adapter.updateData(response.body());
                } else {
                    // Erreur backend
                    String error = ErrorParser.parseErrorMessage(response.errorBody());
                    showError(error);
                }
            }

            @Override
            public void onFailure(Call<DataResponse> call, Throwable t) {
                showLoading(false);
                // Erreur réseau
                String error = ErrorParser.getNetworkErrorMessage(t);
                showError(error);
            }
        });
    }

    // 6. Helpers UI
    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisibility(View.VISIBLE);
    }
}
```

---

## 📝 RÉSUMÉ TECHNIQUE

### SharedPreferences (Stockage Local)

**Données stockées après connexion**:
```java
SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
prefs.edit()
    .putString("jwt_token", "eyJhbGciOiJIUzI1NiJ9...")
    .putString("user_email", "jean@gmail.com")
    .putString("user_role", "PATIENT")  // ou "DOCTOR", "ADMIN"
    .putLong("user_id", 1)
    .putLong("patient_id", 1)  // Seulement si PATIENT
    .putLong("doctor_id", 1)   // Seulement si DOCTOR
    .putString("user_first_name", "Jean")
    .putString("user_last_name", "Dupont")
    .apply();
```

**Utilisation**:
- JWT token → Ajouté automatiquement par JwtInterceptor dans TOUTES les requêtes
- user_role → SplashActivity décide vers quelle home rediriger
- patient_id / doctor_id → Utilisé dans les appels API (getPatientAppointments, getDoctorTimeSlots, etc.)

---

### Retrofit Call Pattern

**Asynchrone (enqueue)**:
```java
Call<LoginResponse> call = apiService.login(loginRequest);
call.enqueue(new Callback<LoginResponse>() {
    @Override
    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        // Thread principal (UI)
        if (response.isSuccessful()) {
            LoginResponse data = response.body();  // Désérialisé par Gson
            // Traiter data
        } else {
            // Erreur HTTP (400, 401, 409, etc.)
        }
    }

    @Override
    public void onFailure(Call<LoginResponse> call, Throwable t) {
        // Erreur réseau (timeout, connexion, etc.)
    }
});
```

---

### RecyclerView Pattern

```java
// 1. Activity: Setup
RecyclerView recyclerView = findViewById(R.id.recycler_view);
recyclerView.setLayoutManager(new LinearLayoutManager(this));  // ou GridLayoutManager
ExampleAdapter adapter = new ExampleAdapter(new ArrayList<>());
recyclerView.setAdapter(adapter);

// 2. Charger données
apiService.getData().enqueue(new Callback<List<DataResponse>>() {
    public void onResponse(...) {
        List<DataResponse> data = response.body();
        adapter.updateData(data);  // Adapter met à jour et rafraîchit
    }
});

// 3. Adapter: Affichage
public class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ViewHolder> {

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataResponse item = data.get(position);
        holder.textView.setText(item.getName());

        holder.itemView.setOnClickListener(v -> {
            // Navigation ou action
        });
    }

    public void updateData(List<DataResponse> newData) {
        this.data = newData;
        notifyDataSetChanged();  // Rafraîchir RecyclerView
    }
}
```

---

## 🎯 POINTS CLÉS À RETENIR

1. **ApiClient** configure Retrofit AVEC JwtInterceptor → Token ajouté automatiquement
2. **ApiService** définit les endpoints → Retrofit génère l'implémentation
3. **Models Request/Response** → Gson sérialise/désérialise JSON automatiquement
4. **SharedPreferences** stocke token + infos user → Persistant entre sessions
5. **SplashActivity** vérifie token au démarrage → Redirige selon rôle
6. **RecyclerView + Adapter** → Pattern standard pour listes performantes
7. **onResume()** → Rafraîchir données après retour d'une activity enfant
8. **ErrorParser** → Messages d'erreur lisibles pour l'utilisateur
9. **Material Components** → UI moderne et cohérente (Toolbar, FAB, CardView, etc.)
10. **Navigation** → Intent avec extras pour passer données entre activities

---

**Ce document couvre 100% des fichiers Android du projet Cabinet Médical.** 🚀
