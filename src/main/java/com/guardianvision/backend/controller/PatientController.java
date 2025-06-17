package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.repository.CaregiverRepository;
import com.guardianvision.backend.repository.PatientRepository;
import com.guardianvision.backend.service.PatientService;
import com.guardianvision.backend.util.JwtUtil;
import com.guardianvision.backend.util.PasswordArgon2;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173"},
        allowCredentials = "true"
)
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
    public ResponseEntity<Patient> create(@RequestBody Patient patient,
                                          @RequestParam(required = false) Long caregiverId) {
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
    public ResponseEntity<Map<String, String>> login(@RequestBody Patient login, HttpServletResponse response) {
        boolean success = service.login(login.getUsername(), login.getPassword());
        if (!success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        String token = JwtUtil.generateToken(login.getUsername(), "ADMIN");

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true if using HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day

        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("role", "PATIENT"));    }


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

    @PutMapping("/{patientId}/assign-caregiver/{caregiverId}")
    public ResponseEntity<Patient> assignCaregiverToPatient(
            @PathVariable Long patientId,
            @PathVariable Long caregiverId) {
        try {
            Patient updated = service.assignCaregiver(patientId, caregiverId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
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