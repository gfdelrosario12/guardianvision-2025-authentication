// AlertController.java
package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Alerts;
import com.guardianvision.backend.service.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Alerts> create(@RequestParam Long patientId, @RequestBody Alerts alert) {
        return new ResponseEntity<>(service.create(patientId, alert), HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Alerts>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.getAllByPatient(patientId));
    }
}
