# 📅 SYSTÈME DE RÉSERVATION - EXPLICATION COMPLÈTE

## 🏗️ ARCHITECTURE GLOBALE

```
PATIENT (Android) → API REST → BACKEND (Spring Boot) → DATABASE (PostgreSQL)
```

---

## 📊 STRUCTURE DE LA BASE DE DONNÉES

### 1. Table `timeslot` (Créneaux fixes des médecins)

```sql
CREATE TABLE timeslot (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,  -- MONDAY, TUESDAY, etc.
    start_time TIME NOT NULL,           -- 09:00:00
    end_time TIME NOT NULL,             -- 12:00:00
    created_at TIMESTAMP
);
```

**Exemple de données:**
```
id | doctor_id | day_of_week | start_time | end_time  | created_at
---|-----------|-------------|------------|-----------|------------------
1  | 1         | MONDAY      | 09:00:00   | 12:00:00  | 2026-01-31 16:00
2  | 1         | MONDAY      | 14:00:00   | 18:00:00  | 2026-01-31 16:00
3  | 1         | WEDNESDAY   | 09:00:00   | 12:00:00  | 2026-01-31 16:00
4  | 1         | FRIDAY      | 09:00:00   | 18:00:00  | 2026-01-31 16:00
5  | 2         | TUESDAY     | 09:00:00   | 12:00:00  | 2026-01-31 16:00
6  | 2         | THURSDAY    | 14:00:00   | 18:00:00  | 2026-01-31 16:00
```

**Signification:**
- Dr. Saad (ID=1) travaille:
  - Lundi: 9h-12h ET 14h-18h
  - Mercredi: 9h-12h
  - Vendredi: 9h-18h
- Dr. Oussama (ID=2) travaille:
  - Mardi: 9h-12h
  - Jeudi: 14h-18h

### 2. Table `appointment` (Rendez-vous réservés)

```sql
CREATE TABLE appointment (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    date_time TIMESTAMP NOT NULL,       -- 2026-02-03 14:00:00
    reason VARCHAR(500),                -- "Consultation générale"
    status VARCHAR(20) NOT NULL,        -- PENDING, CONFIRMED, CANCELLED
    cancelled_by VARCHAR(20),           -- PATIENT, DOCTOR, ADMIN (si annulé)
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Exemple:**
```
id | patient_id | doctor_id | date_time           | reason          | status
---|------------|-----------|---------------------|-----------------|--------
1  | 1          | 1         | 2026-02-03 14:00:00 | TEST MOTIF      | PENDING
```

---

## 🔄 FLOW COMPLET DE RÉSERVATION

### ÉTAPE 1: Patient sélectionne un médecin

**Android:**
```java
// DoctorListActivity
GET /api/doctors
→ Affiche liste de médecins
→ Patient clique sur "Dr. Saad Doctor"
→ Ouvre AvailableTimeSlotsActivity
```

**Backend:**
```java
@GetMapping("/api/doctors")
public List<DoctorResponse> getAllDoctors() {
    return doctorService.getAllDoctors();
}
```

### ÉTAPE 2: Patient sélectionne une date

**Android:**
```java
// AvailableTimeSlotsActivity
CalendarView affiche calendrier
→ setMinDate(today) bloque les dates passées
→ Patient sélectionne 3 février 2026 (LUNDI)
```

**Pourquoi le 3 février?**
- 1er février 2026 = SAMEDI → Pas de timeslots (médecins ne travaillent pas)
- 2 février 2026 = DIMANCHE → Pas de timeslots
- 3 février 2026 = **LUNDI** → Dr. Saad a des timeslots ce jour-là ✅

### ÉTAPE 3: Backend calcule les créneaux disponibles

**Android:**
```java
GET /api/timeslots/available?doctorId=1&date=2026-02-03
```

**Backend (TimeSlotService.java):**
```java
public List<LocalTime> getAvailableTimesForDate(Long doctorId, LocalDate date) {
    // 1. Récupérer le jour de la semaine
    DayOfWeek dayOfWeek = date.getDayOfWeek(); // MONDAY

    // 2. Charger les timeslots du médecin pour ce jour
    List<TimeSlot> timeSlots = timeslotRepository
        .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek.toString());
    // → [09:00-12:00, 14:00-18:00]

    // 3. Générer tous les slots possibles (intervalles de 30 min)
    List<LocalTime> allPossibleTimes = new ArrayList<>();
    for (TimeSlot slot : timeSlots) {
        LocalTime current = slot.getStartTime(); // 09:00
        while (current.isBefore(slot.getEndTime())) {
            allPossibleTimes.add(current);
            current = current.plusMinutes(30); // 09:30, 10:00, 10:30, ...
        }
    }
    // → [09:00, 09:30, 10:00, 10:30, 11:00, 11:30, 14:00, 14:30, ..., 17:30]

    // 4. Récupérer les RDV déjà réservés pour ce jour
    List<Appointment> bookedAppointments = appointmentRepository
        .findByDoctorIdAndDate(doctorId, date);
    // → Si un RDV à 09:30 existe → [09:30]

    // 5. Enlever les heures réservées
    Set<LocalTime> bookedTimes = bookedAppointments.stream()
        .map(apt -> apt.getDateTime().toLocalTime())
        .collect(Collectors.toSet());

    allPossibleTimes.removeIf(time -> bookedTimes.contains(time));

    // 6. Retourner les heures disponibles
    return allPossibleTimes;
    // → [09:00, 10:00, 10:30, 11:00, 11:30, 14:00, ...] (sans 09:30)
}
```

**Réponse API:**
```json
[
  "09:00:00",
  "09:30:00",
  "10:00:00",
  "10:30:00",
  "11:00:00",
  "11:30:00",
  "14:00:00",
  "14:30:00",
  "15:00:00",
  "15:30:00",
  "16:00:00",
  "16:30:00",
  "17:00:00",
  "17:30:00"
]
```

**Android affiche:**
```
┌─────────┬─────────┬─────────┐
│  09:00  │  09:30  │  10:00  │
├─────────┼─────────┼─────────┤
│  10:30  │  11:00  │  11:30  │
├─────────┼─────────┼─────────┤
│  14:00  │  14:30  │  15:00  │
└─────────┴─────────┴─────────┘
```

### ÉTAPE 4: Patient sélectionne une heure

**Android:**
```java
// Patient clique sur 11:00
→ Ouvre BookAppointmentActivity
→ Affiche résumé:
   - Médecin: Dr. Saad Doctor
   - Date/Heure: 03/02/2026 à 11:00
   - Champ: Motif de consultation
```

### ÉTAPE 5: Patient confirme le rendez-vous

**Android:**
```java
POST /api/appointments?patientId=1
Body: {
  "doctorId": 1,
  "dateTime": "2026-02-03T11:00:00",
  "reason": "TEST MOTIF"
}
```

**Backend (AppointmentService.java):**
```java
public AppointmentResponse createAppointment(Long patientId, CreateAppointmentRequest request) {
    // 1. VALIDATION: Date dans le futur?
    LocalDateTime appointmentDateTime = LocalDateTime.parse(request.getDateTime());
    if (appointmentDateTime.isBefore(LocalDateTime.now())) {
        throw new BadRequestException("La date du rendez-vous doit être dans le futur");
    }

    // 2. VALIDATION: Patient existe?
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé"));

    // 3. VALIDATION: Médecin existe?
    Doctor doctor = doctorRepository.findById(request.getDoctorId())
        .orElseThrow(() -> new ResourceNotFoundException("Médecin non trouvé"));

    // 4. VALIDATION: Créneau disponible? (RG-02)
    boolean isSlotTaken = appointmentRepository.existsByDoctorIdAndDateTime(
        request.getDoctorId(),
        appointmentDateTime
    );
    if (isSlotTaken) {
        throw new ConflictException("Ce créneau est déjà réservé");
    }

    // 5. Créer le rendez-vous
    Appointment appointment = new Appointment();
    appointment.setPatient(patient);
    appointment.setDoctor(doctor);
    appointment.setDateTime(appointmentDateTime);
    appointment.setReason(request.getReason());
    appointment.setStatus(Appointment.Status.PENDING);
    appointment.setCreatedAt(LocalDateTime.now());
    appointment.setUpdatedAt(LocalDateTime.now());

    // 6. Sauvegarder
    Appointment saved = appointmentRepository.save(appointment);

    // 7. Retourner réponse
    return AppointmentResponse.from(saved);
}
```

**Réponse API (201 CREATED):**
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Fatima Patient",
  "doctorId": 1,
  "doctorName": "Dr. Saad Doctor",
  "doctorSpecialty": "Pédiatre",
  "dateTime": "2026-02-03T11:00:00",
  "reason": "TEST MOTIF",
  "status": "PENDING",
  "createdAt": "2026-01-31T18:30:00",
  "updatedAt": "2026-01-31T18:30:00"
}
```

**Android:**
```java
// Succès!
Toast.makeText("Rendez-vous confirmé !");
→ Retour à HomePatientActivity
```

---

## ❓ POURQUOI CERTAINES DATES N'ONT PAS DE CRÉNEAUX?

### Cas 1: Jour de la semaine sans timeslots

```
Samedi 1er février 2026 → Aucun timeslot dans la DB
→ Backend retourne: []
→ Android affiche: "Aucun créneau disponible pour cette date"
```

### Cas 2: Jour avec timeslots mais tous réservés

```
Lundi 3 février 2026 → Timeslots: [09:00, 09:30, 10:00, ...]
Rendez-vous existants: [09:00, 09:30, 10:00, 10:30, 11:00, ...]
→ Backend retourne: [] (tous occupés)
→ Android affiche: "Aucun créneau disponible pour cette date"
```

### Cas 3: Date passée (corrigé maintenant)

```
Jeudi 30 janvier 2026 (hier)
→ CalendarView.setMinDate() empêche la sélection
→ L'utilisateur ne peut pas cliquer dessus
```

---

## 🐛 BUG CORRIGÉ: "Mes rendez-vous" crashait

**Problème:**
```java
// AppointmentAdapter.java (AVANT)
statusColor = holder.itemView.getContext().getColor(R.color.status_pending);
// ❌ getColor() nécessite API 23+ (Android 6.0+)
```

**Solution:**
```java
// AppointmentAdapter.java (APRÈS)
statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
// ✅ Compatible avec toutes les versions Android
```

---

## 📱 FLOW COMPLET DANS L'APPLICATION

```
1. Login → HomePatientActivity
   ├─ "Voir les médecins" → DoctorListActivity
   │   └─ Clic médecin → AvailableTimeSlotsActivity
   │       ├─ Sélection date (CalendarView)
   │       │   └─ Dates passées: BLOQUÉES ✅
   │       │   └─ Weekend: Affiche "Aucun créneau disponible"
   │       │   └─ Jour avec timeslots: Affiche grille 3 colonnes
   │       └─ Clic heure → BookAppointmentActivity
   │           ├─ Résumé: Médecin + Date + Heure
   │           ├─ Input: Motif (obligatoire)
   │           └─ Confirmer → POST /api/appointments
   │               ├─ Succès: Toast + Retour Home
   │               └─ Erreur: Message d'erreur affiché
   │
   └─ "Mes rendez-vous" → MyAppointmentsActivity
       └─ Affiche liste de tous les RDV du patient
           ├─ Status PENDING: Orange 🟠
           ├─ Status CONFIRMED: Vert 🟢
           └─ Status CANCELLED: Rouge 🔴
```

---

## 🎯 RÉSUMÉ DES RÈGLES MÉTIER

1. **RG-01:** Rendez-vous créés avec status PENDING par défaut
2. **RG-02:** Un seul rendez-vous par créneau médecin (UNIQUE constraint)
3. **RG-03:** Date/heure doit être dans le futur
4. **RG-04:** Créneau doit exister dans les timeslots du médecin
5. **RG-05:** Médecins travaillent seulement les jours définis dans timeslot
6. **RG-06:** Intervalles de 30 minutes entre chaque slot

---

## 🔧 PROCHAINES AMÉLIORATIONS POSSIBLES

1. **Notifications:**
   - Email de confirmation après réservation
   - Rappel 24h avant le RDV

2. **Statuts:**
   - Permettre au médecin de confirmer un RDV (PENDING → CONFIRMED)
   - Permettre l'annulation (status → CANCELLED)

3. **Filtres:**
   - "Mes rendez-vous" → Filtrer par status (Tous, En attente, Confirmés, Annulés)
   - Trier par date (Prochains d'abord, Passés d'abord)

4. **UI:**
   - Afficher le prochain RDV sur HomePatientActivity
   - Badge avec nombre de RDV en attente

---

## ✅ CORRECTIONS APPORTÉES AUJOURD'HUI

1. ✅ **CalendarView:** Dates passées maintenant bloquées
2. ✅ **AppointmentAdapter:** Utilise ContextCompat pour compatibilité
3. ✅ **API createAppointment:** Maintenant retourne AppointmentResponse au lieu de Map

---

Date: 2026-01-31
Auteur: Claude Code
