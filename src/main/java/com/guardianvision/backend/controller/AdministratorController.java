package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Administrator;
import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.service.AdministratorService;
import com.guardianvision.backend.service.CaregiverService;
import com.guardianvision.backend.service.PatientService;
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
        origins = {"http://localhost:3000", "http://localhost:5173"},
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

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);

        response.addCookie(cookie);

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


    // ✅ Generic update info (name/email)
    @PutMapping("/users/{role}/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable String role,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return switch (role.toLowerCase()) {
            case "admin" -> ResponseEntity.ok(adminService.updateBasicInfo(id, (Administrator) body));
            case "caregiver" -> ResponseEntity.ok(caregiverService.updateBasicInfo(id, (Caregiver) body));
            case "patient" -> ResponseEntity.ok(patientService.updateBasicInfo(id, (Patient) body));
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
