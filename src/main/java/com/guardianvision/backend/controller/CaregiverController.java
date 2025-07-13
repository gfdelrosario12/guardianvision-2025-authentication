package com.guardianvision.backend.controller;

import com.guardianvision.backend.dto.CaregiverDto;
import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.service.CaregiverService;
import com.guardianvision.backend.util.JwtCookieUtil;
import com.guardianvision.backend.util.JwtUtil;
import com.guardianvision.backend.util.PasswordArgon2;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/caregivers")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "https://guardian-vision.vercel.app", "https://guardian-vision-kkp7d4gui-gladwin-ferdz-del-rosarios-projects.vercel.app"},
        allowCredentials = "true"
)
public class CaregiverController {

    private final CaregiverService service;

    public CaregiverController(CaregiverService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentCaregiver(@CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: No token");
        }

        String username;
        try {
            username = JwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Caregiver caregiver = service.getByUsername(username);
        if (caregiver == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caregiver not found");
        }

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", caregiver.getId());
        dto.put("username", caregiver.getUsername());
        dto.put("email", caregiver.getEmail());
        dto.put("firstName", caregiver.getFirstName());
        dto.put("middleName", caregiver.getMiddleName());
        dto.put("lastName", caregiver.getLastName());
        dto.put("gender", caregiver.getGender());
        dto.put("mobileNumber", caregiver.getMobile_number());
        dto.put("address", caregiver.getAddress());
        dto.put("role", caregiver.getRole());

        // 👇 Include full patient info
        if (caregiver.getPatients() != null) {
            dto.put("patients", caregiver.getPatients().stream().map(p -> {
                Map<String, Object> patientDto = new HashMap<>();
                patientDto.put("id", p.getId());
                patientDto.put("username", p.getUsername());
                patientDto.put("email", p.getEmail());
                patientDto.put("password", p.getPassword());
                patientDto.put("salt", p.getSalt());
                patientDto.put("firstName", p.getFirst_name());
                patientDto.put("middleName", p.getMiddle_name());
                patientDto.put("lastName", p.getLastName());
                patientDto.put("age", p.getAge());
                patientDto.put("height", p.getHeight());
                patientDto.put("weight", p.getWeight());
                patientDto.put("address", p.getAddress());
                patientDto.put("gender", p.getGender());
                patientDto.put("mobile_number", p.getMobile_number());
                patientDto.put("role", p.getRole());
                patientDto.put("emergency_contact_name", p.getEmergencyContactName());
                patientDto.put("emergency_contact_number", p.getEmergencyContactDetails());
                patientDto.put("emergency_contact_address", p.getEmergencyContactAddress());
                patientDto.put("imageUrl", p.getImageUrl());
                return patientDto;
            }).toList());
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ping")
    public String hello() {
        return "Application is running!";
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<Caregiver> caregivers = service.getAll();
        List<Map<String, Object>> simplified = caregivers.stream().map(c -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", c.getId());
            dto.put("first_name", c.getFirstName());
            dto.put("last_name", c.getLastName());
            return dto;
        }).toList();
        return new ResponseEntity<>(simplified, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Caregiver caregiver = service.getById(id); // Use getById, not getDtoById
        if (caregiver == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Caregiver not found"));
        }

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", caregiver.getId());
        dto.put("username", caregiver.getUsername());
        dto.put("email", caregiver.getEmail());
        dto.put("firstName", caregiver.getFirstName());
        dto.put("middleName", caregiver.getMiddleName());
        dto.put("lastName", caregiver.getLastName());
        dto.put("gender", caregiver.getGender());
        dto.put("mobileNumber", caregiver.getMobile_number());
        dto.put("address", caregiver.getAddress());
        dto.put("role", caregiver.getRole());

        // Optional: include assigned patients' IDs or basic info if needed
        if (caregiver.getPatients() != null) {
            dto.put("patients", caregiver.getPatients().stream().map(p -> Map.of(
                    "id", p.getId(),
                    "firstName", p.getFirst_name(),
                    "lastName", p.getLastName()
            )).toList());
        }

        return ResponseEntity.ok(dto);
    }


    @PostMapping
    public ResponseEntity<Caregiver> create(@RequestBody Caregiver caregiver) {
        PasswordArgon2 argon2 = new PasswordArgon2();
        String salt = argon2.generateSalt();
        String hashed = argon2.encryptPassword(caregiver.getPassword(), salt);
        caregiver.setSalt(salt);
        caregiver.setPassword(hashed);
        caregiver.setRole("CAREGIVER");
        Long lastID = service.getLastInsertedId();
        String username = service.username(lastID);
        caregiver.setUsername(username);
        return new ResponseEntity<>(service.create(caregiver), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Caregiver login, HttpServletResponse response) {
        Caregiver caregiver = service.verifyLoginAndReturnUser(login.getUsername(), login.getPassword());
        if (caregiver == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        String token = JwtUtil.generateTokenWithClaims(Map.of(
                "username", caregiver.getUsername(),
                "role", "CAREGIVER",
                "userId", caregiver.getId()
        ));

        JwtCookieUtil.setJwtCookie(response, token);

        return ResponseEntity.ok(Map.of("role", "CAREGIVER"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Caregiver> update(@PathVariable Long id, @RequestBody Caregiver caregiver) {
        Caregiver updated = service.update(id, caregiver);
        return updated != null ? new ResponseEntity<>(updated, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        Caregiver updated = service.changePassword(id, newPassword);
        if (updated != null)
            return ResponseEntity.ok(updated);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrator not found");
    }

}