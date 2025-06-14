package com.guardianvision.backend.service;

import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.repository.PatientRepository;
import com.guardianvision.backend.repository.CaregiverRepository;
import com.guardianvision.backend.util.PasswordArgon2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository patientRepo;
    private final CaregiverRepository caregiverRepo;

    public PatientService(PatientRepository patientRepo, CaregiverRepository caregiverRepo) {
        this.patientRepo = patientRepo;
        this.caregiverRepo = caregiverRepo;
    }

    public List<Patient> getAll() {
        return patientRepo.findAll();
    }

    public Patient getById(Long id) {
        return patientRepo.findById(id).orElse(null);
    }

    public Patient create(Patient patient, Long caregiverId) {
        Caregiver caregiver = caregiverRepo.findById(caregiverId).orElseThrow();
        patient.setCaregiver(caregiver);
        return patientRepo.save(patient);
    }

    public Patient update(Long id, Patient updated) {
        return patientRepo.findById(id).map(patient -> {
            patient.setUsername(updated.getUsername());
            patient.setEmail(updated.getEmail());
            patient.setFull_name(updated.getFull_name());
            patient.setAddress(updated.getAddress());
            patient.setGender(updated.getGender());
            patient.setMobile_number(updated.getMobile_number());
            return patientRepo.save(patient);
        }).orElse(null);
    }

    public boolean login(String username, String rawPassword) {
        Patient patient = patientRepo.findByUsername(username);
        if (patient == null) return false;
        PasswordArgon2 argon2 = new PasswordArgon2();
        return argon2.matchPasswords(rawPassword, patient.getSalt(), patient.getPassword());
    }

    public void delete(Long id) {
        patientRepo.deleteById(id);
    }

    public List<Patient> getByCaregiverId(Long caregiverId) {
        return patientRepo.findByCaregiverId(caregiverId);
    }

    public Patient changePassword(Long id, String newPassword) {
        return patientRepo.findById(id).map(admin -> {
            String newSalt = PasswordArgon2.generateSalt();
            String newHash = PasswordArgon2.encryptPassword(newPassword, newSalt);
            admin.setSalt(newSalt);
            admin.setPassword(newHash);
            return patientRepo.save(admin);
        }).orElse(null);
    }

}