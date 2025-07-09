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

@Service
public class AlertService {
    private final AlertRepository alertRepo;
    private final PatientRepository patientRepo;

    public AlertService(AlertRepository alertRepo, PatientRepository patientRepo) {
        this.alertRepo = alertRepo;
        this.patientRepo = patientRepo;
    }

    public Alerts create(Long patientId, Alerts alert) {
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        alert.setPatient(patient);

        if (alert.getTimestamp() == null) {
            alert.setTimestamp(OffsetDateTime.now().toString());
        }
        // if timestamp is "" (empty string), do nothing (keep it as empty)

        return alertRepo.save(alert);
    }

    public List<Alerts> getAllByPatient(Long patientId) {
        return alertRepo.findByPatientId(patientId);
    }
}
