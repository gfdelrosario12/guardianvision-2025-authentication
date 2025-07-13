// AlertService.java
package com.guardianvision.backend.service;

import com.guardianvision.backend.entity.Alerts;
import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.repository.AlertRepository;
import com.guardianvision.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Async;

@Service
public class AlertService {
    private final AlertRepository alertRepo;
    private final PatientRepository patientRepo;

    public AlertService(AlertRepository alertRepo, PatientRepository patientRepo) {
        this.alertRepo = alertRepo;
        this.patientRepo = patientRepo;
    }

    // Original method remains if you need sync usage
    public Alerts create(Long patientId, Alerts alert) {
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        alert.setPatient(patient);

        if (alert.getTimestamp() == null) {
            alert.setTimestamp(OffsetDateTime.now().toString());
        }

        return alertRepo.save(alert);
    }

    @Async
    public void createAsync(Long patientId, Alerts alert) {
        create(patientId, alert); // reuse existing method
        System.out.println("📌 [Async] Alert saved for patient " + patientId);
    }

    public List<Alerts> getAllByPatient(Long patientId) {
        return alertRepo.findByPatientId(patientId);
    }
}
