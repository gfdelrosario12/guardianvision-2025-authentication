package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Administrator;
import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.service.AdministratorService;
import com.guardianvision.backend.service.CaregiverService;
import com.guardianvision.backend.service.PatientService;
import com.guardianvision.backend.util.JwtCookieUtil;
import com.guardianvision.backend.util.JwtUtil;
import com.guardianvision.backend.util.PasswordArgon2;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "https://guardian-vision.vercel.app", "https://guardian-vision-kkp7d4gui-gladwin-ferdz-del-rosarios-projects.vercel.app"},
        allowCredentials = "true"
)
public class AdministratorController {

    private final AdministratorService adminService;
    private final CaregiverService caregiverService;
    private final PatientService patientService;

    public AdministratorController(
            AdministratorService adminService,
            CaregiverService caregiverService,
            PatientService patientService
    ) {
        this.adminService = adminService;
        this.caregiverService = caregiverService;
        this.patientService = patientService;
    }

    @GetMapping("/ping")
    public String hello() {
        return "Application is running!";
    }

    // ✅ Create Administrator
    @PostMapping
    public ResponseEntity<Administrator> create(@RequestBody Administrator admin) {
        PasswordArgon2 argon2 = new PasswordArgon2();
        String salt = argon2.generateSalt();
        String hashed = argon2.encryptPassword(admin.getPassword(), salt);
        admin.setSalt(salt);
        admin.setPassword(hashed);
        admin.setRole("ADMIN");

        Long lastID = adminService.getLastInsertedId();
        String username = adminService.username(lastID);
        admin.setUsername(username);

        return new ResponseEntity<>(adminService.create(admin), HttpStatus.CREATED);
    }

    // ✅ Login and Set Cookie
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Administrator login, HttpServletResponse response) {
        boolean success = adminService.login(login.getUsername(), login.getPassword());
        if (!success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        String token = JwtUtil.generateToken(login.getUsername(), "ADMIN");

        JwtCookieUtil.setJwtCookie(response, token);

        return ResponseEntity.ok(Map.of("role", "ADMIN"));
    }

    // ✅ Get current admin info
    @GetMapping("/me")
    public ResponseEntity<Administrator> getMe(@CookieValue("jwt") String token) {
        String username = JwtUtil.getUsernameFromToken(token);
        return ResponseEntity.ok(adminService.findByUsername(username));
    }

    // ✅ Get all users (admin, caregiver, patient)
    @GetMapping("/users")
    public ResponseEntity<Map<String, List<?>>> getAllUsers() {
        Map<String, List<?>> result = new HashMap<>();
        result.put("admins", adminService.getAll());
        result.put("caregivers", caregiverService.getAll());
        result.put("patients", patientService.getAll());
        return ResponseEntity.ok(result);
    }


    @PutMapping("/users/{role}/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String role,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return switch (role.toLowerCase()) {
            case "admin" -> {
                Administrator admin = new Administrator();
                admin.setFirstName(body.get("firstName"));
                admin.setMiddleName(body.get("middleName"));
                admin.setLastName(body.get("lastName"));
                admin.setEmail(body.get("email"));
                admin.setRole(body.get("role"));
                yield ResponseEntity.ok(adminService.updateBasicInfo(id, admin));
            }
            case "caregiver" -> {
                Caregiver caregiver = new Caregiver();
                caregiver.setFirstName(body.get("firstName"));
                caregiver.setMiddleName(body.get("middleName"));
                caregiver.setLastName(body.get("lastName"));
                caregiver.setEmail(body.get("email"));
                caregiver.setRole(body.get("role"));
                yield ResponseEntity.ok(caregiverService.updateBasicInfo(id, caregiver));
            }
            case "patient" -> {
                Patient patient = new Patient();
                patient.setFirst_name(body.get("firstName"));
                patient.setMiddle_name(body.get("middleName"));
                patient.setLastName(body.get("lastName"));
                patient.setEmail(body.get("email"));
                patient.setRole(body.get("role"));
                yield ResponseEntity.ok(patientService.updateBasicInfo(id, patient));
            }
            default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role");
        };
    }

    // ✅ Generic password change
    @PutMapping("/users/{role}/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable String role,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String newPassword = body.get("newPassword");

        switch (role.toLowerCase()) {
            case "admin":
                return ResponseEntity.ok(adminService.changePassword(id, newPassword));
            case "caregiver":
                return ResponseEntity.ok(caregiverService.changePassword(id, newPassword));
            case "patient":
                return ResponseEntity.ok(patientService.changePassword(id, newPassword));
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role");
        }
    }

    // ✅ Generic delete
    @DeleteMapping("/users/{role}/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String role, @PathVariable Long id) {
        switch (role.toLowerCase()) {
            case "admin":
                adminService.delete(id);
                break;
            case "caregiver":
                caregiverService.delete(id);
                break;
            case "patient":
                patientService.delete(id);
                break;
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role");
        }
        return ResponseEntity.noContent().build();
    }
}
