package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.repository.CaregiverRepository;
import com.guardianvision.backend.repository.PatientRepository;
import com.guardianvision.backend.service.PatientService;
import com.guardianvision.backend.util.PasswordArgon2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:5173")
public class PatientController {

    private final PatientRepository patientRepo;
    private final CaregiverRepository caregiverRepo;
    private final PatientService service;

    public PatientController(PatientRepository patientRepo, CaregiverRepository caregiverRepo, PatientService service) {
        this.patientRepo = patientRepo;
        this.caregiverRepo = caregiverRepo;
        this.service = service;
    }

    @GetMapping("/ping")
    public String hello() {
        return "Application is running!";
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getAll() {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @GetMapping("/caregiver/{caregiverId}")
    public ResponseEntity<List<Patient>> getByCaregiver(@PathVariable Long caregiverId) {
        return new ResponseEntity<>(service.getByCaregiverId(caregiverId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Patient> create(@RequestParam Long caregiverId, @RequestBody Patient patient) {
        PasswordArgon2 argon2 = new PasswordArgon2();
        String salt = argon2.generateSalt();
        String hashed = argon2.encryptPassword(patient.getPassword(), salt);
        patient.setSalt(salt);
        patient.setPassword(hashed);
        patient.setRole("PATIENT");
        Long lastID = service.getLastInsertedId();
        String username = service.username(lastID);
        patient.setUsername(username);
        return new ResponseEntity<>(service.create(patient, caregiverId), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Patient login) {
        boolean success = service.login(login.getUsername(), login.getPassword());
        return success ? ResponseEntity.ok("Login successful") : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Long id, @RequestBody Patient patient) {
        Patient updated = service.update(id, patient);
        return updated != null ? new ResponseEntity<>(updated, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public Patient assignCaregiver(Long patientId, Long caregiverId) {
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        Caregiver caregiver = caregiverRepo.findById(caregiverId).orElseThrow();
        patient.setCaregiver(caregiver);
        return patientRepo.save(patient);
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        Patient updated = service.changePassword(id, newPassword);
        if (updated != null)
            return ResponseEntity.ok(updated);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrator not found");
    }

}