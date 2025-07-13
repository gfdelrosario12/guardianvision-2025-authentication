// OutageController.java
package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Outages;
import com.guardianvision.backend.service.OutageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/outages")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "https://guardian-vision.vercel.app", "https://guardian-vision-kkp7d4gui-gladwin-ferdz-del-rosarios-projects.vercel.app"},
        allowCredentials = "true"
)
public class OutageController {
    private final OutageService service;

    public OutageController(OutageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Outages> create(@RequestParam Long patientId, @RequestBody Outages outage) {
        return new ResponseEntity<>(service.create(patientId, outage), HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Outages>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.getAllByPatient(patientId));
    }
}
