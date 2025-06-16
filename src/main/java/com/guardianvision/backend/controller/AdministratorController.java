package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Administrator;
import com.guardianvision.backend.service.AdministratorService;
import com.guardianvision.backend.util.PasswordArgon2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "http://localhost:5173")
public class AdministratorController {

    private final AdministratorService service;

    public AdministratorController(AdministratorService service) {
        this.service = service;
    }

    @GetMapping("/ping")
    public String hello() {
        return "Application is running!";
    }

    @GetMapping
    public ResponseEntity<List<Administrator>> getAll() {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Administrator> getById(@PathVariable Long id) {
        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Administrator> create(@RequestBody Administrator admin) {
        PasswordArgon2 argon2 = new PasswordArgon2();
        String salt = argon2.generateSalt();
        String hashed = argon2.encryptPassword(admin.getPassword(), salt);
        admin.setSalt(salt);
        admin.setPassword(hashed);
        admin.setRole("ADMIN"); // Set default role

        Long lastID = service.getLastInsertedId();
        String username = service.username(lastID);
        admin.setUsername(username);

        return new ResponseEntity<>(service.create(admin), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Administrator login) {
        boolean success = service.login(login.getUsername(), login.getPassword());
        return success
                ? ResponseEntity.ok("Login successful")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Administrator> update(@PathVariable Long id, @RequestBody Administrator admin) {
        Administrator updated = service.update(id, admin);
        return updated != null
                ? new ResponseEntity<>(updated, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        Administrator updated = service.changePassword(id, newPassword);
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrator not found");
    }
}
