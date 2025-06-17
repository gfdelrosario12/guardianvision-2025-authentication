// OutageService.java
package com.guardianvision.backend.service;

import com.guardianvision.backend.entity.Outages;
import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.repository.OutageRepository;
import com.guardianvision.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutageService {
    private final OutageRepository outageRepo;
    private final PatientRepository patientRepo;

    public OutageService(OutageRepository outageRepo, PatientRepository patientRepo) {
        this.outageRepo = outageRepo;
        this.patientRepo = patientRepo;
    }

    public Outages create(Long patientId, Outages outage) {
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        outage.setPatient(patient);
        outage.setTimestamp(LocalDateTime.now());
        return outageRepo.save(outage);
    }

    public List<Outages> getAllByPatient(Long patientId) {
        return outageRepo.findByPatientId(patientId);
    }
}
