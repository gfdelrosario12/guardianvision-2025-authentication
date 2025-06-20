package com.guardianvision.backend.service;

import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.repository.PatientRepository;
import com.guardianvision.backend.repository.CaregiverRepository;
import com.guardianvision.backend.util.PasswordArgon2;
import org.springframework.data.domain.Sort;
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

    // ✅ Fixed method with caregiverId parameter
    public Patient create(Patient patient, Long caregiverId) {
        if (caregiverId != null) {
            Caregiver caregiver = caregiverRepo.findById(caregiverId)
                    .orElseThrow(() -> new RuntimeException("Caregiver not found with ID: " + caregiverId));
            patient.setCaregiver(caregiver);
        }

        // Generate username and hash password
        Long newId = getLastInsertedId();
        String username = username(newId);
        patient.setUsername(username);

        String salt = PasswordArgon2.generateSalt();
        String hashedPassword = PasswordArgon2.encryptPassword(patient.getPassword(), salt);
        patient.setSalt(salt);
        patient.setPassword(hashedPassword);

        return patientRepo.save(patient);
    }

    public Patient update(Long id, Patient updated) {
        return patientRepo.findById(id).map(patient -> {
            patient.setEmail(updated.getEmail());
            patient.setFirst_name(updated.getFirst_name());
            patient.setMiddle_name(updated.getMiddle_name());
            patient.setLastName(updated.getLastName());
            patient.setAddress(updated.getAddress());
            patient.setGender(updated.getGender());
            patient.setMobile_number(updated.getMobile_number());
            patient.setAge(updated.getAge());
            patient.setHeight(updated.getHeight());
            patient.setWeight(updated.getWeight());
            patient.setEmergencyContactName(updated.getEmergencyContactName());
            patient.setEmergencyContactDetails(updated.getEmergencyContactDetails());
            patient.setEmergencyContactAddress(updated.getEmergencyContactAddress());
            patient.setImageUrl(updated.getImageUrl()); // 🔥 Add this line
            return patientRepo.save(patient);
        }).orElse(null);
    }

    public boolean login(String username, String rawPassword) {
        Patient patient = patientRepo.findByUsername(username);
        if (patient == null) return false;
        PasswordArgon2 argon2 = new PasswordArgon2();
        return argon2.matchPasswords(patient.getSalt(), rawPassword, patient.getPassword());
    }

    public void delete(Long id) {
        patientRepo.deleteById(id);
    }

    public List<Patient> getByCaregiverId(Long caregiverId) {
        return patientRepo.findByCaregiverId(caregiverId);
    }

    public Patient changePassword(Long id, String newPassword) {
        return patientRepo.findById(id).map(patient -> {
            String newSalt = PasswordArgon2.generateSalt();
            String newHash = PasswordArgon2.encryptPassword(newPassword, newSalt);
            patient.setSalt(newSalt);
            patient.setPassword(newHash);
            return patientRepo.save(patient);
        }).orElse(null);
    }

    public Long getLastInsertedId() {
        List<Patient> entities = patientRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        if (!entities.isEmpty()) {
            return entities.get(0).getId() + 1;
        } else {
            return 1L;
        }

    }

    public String username(Long lastID) {
        return "GV - " + "PT" + " - " + lastID;
    }

    public Patient assignCaregiver(Long patientId, Long caregiverId) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Caregiver caregiver = caregiverRepo.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("Caregiver not found"));
        patient.setCaregiver(caregiver);
        return patientRepo.save(patient);
    }

    public Patient getByUsername(String username) {
        return patientRepo.findByUsername(username);
    }

    public Patient updateBasicInfo(Long id, Patient updated) {
        return patientRepo.findById(id).map(patient -> {
            patient.setFirst_name(updated.getFirst_name());
            patient.setMiddle_name(updated.getMiddle_name());
            patient.setLastName(updated.getLastName());
            patient.setEmail(updated.getEmail());
            patient.setMobile_number(updated.getMobile_number());
            patient.setGender(updated.getGender());
            patient.setAddress(updated.getAddress());
            return patientRepo.save(patient);
        }).orElse(null);
    }

}
