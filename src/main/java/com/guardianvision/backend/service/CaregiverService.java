package com.guardianvision.backend.service;

import com.guardianvision.backend.dto.CaregiverDto;
import com.guardianvision.backend.dto.PatientSummaryDto;
import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.repository.CaregiverRepository;
import com.guardianvision.backend.util.PasswordArgon2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CaregiverService {
    private final CaregiverRepository repo;

    public CaregiverService(CaregiverRepository repo) {
        this.repo = repo;
    }

    public List<Caregiver> getAll() {
        return repo.findAll();
    }

    public Caregiver getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Caregiver create(Caregiver caregiver) {
        return repo.save(caregiver);
    }

    public Caregiver update(Long id, Caregiver updated) {
        return repo.findById(id).map(caregiver -> {
            caregiver.setEmail(updated.getEmail());
            caregiver.setFirstName(updated.getFirstName());
            caregiver.setFirstName(updated.getFirstName());
            caregiver.setMiddleName(updated.getMiddleName());
            caregiver.setLastName(updated.getLastName());
            caregiver.setAddress(updated.getAddress());
            caregiver.setGender(updated.getGender());
            caregiver.setMobile_number(updated.getMobile_number());
            return repo.save(caregiver);
        }).orElse(null);
    }


    public Caregiver verifyLoginAndReturnUser(String username, String rawPassword) {
        Caregiver caregiver = repo.findByUsername(username);
        if (caregiver == null) return null;

        PasswordArgon2 argon2 = new PasswordArgon2();
        boolean matches = argon2.matchPasswords(caregiver.getSalt(), rawPassword, caregiver.getPassword());

        return matches ? caregiver : null;
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Caregiver changePassword(Long id, String newPassword) {
        return repo.findById(id).map(admin -> {
            String newSalt = PasswordArgon2.generateSalt();
            String newHash = PasswordArgon2.encryptPassword(newPassword, newSalt);
            admin.setSalt(newSalt);
            admin.setPassword(newHash);
            return repo.save(admin);
        }).orElse(null);
    }

    public Long getLastInsertedId() {
        // Assuming your id field is Long and auto-generated
        // You can fetch the last inserted id by sorting in descending order
        List<Caregiver> entities = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));

        if (!entities.isEmpty()) {
            return entities.get(0).getId() + 1;
        } else {
            return 1L;
        }
    }

    public String username(Long lastID) {
        return "GV - " + "CC" + " - " + lastID;
    }

    public Caregiver getByUsername(String username) {
        return repo.findByUsername(username);
    }

    public Caregiver updateBasicInfo(Long id, Caregiver updated) {
        return repo.findById(id).map(caregiver -> {
            caregiver.setFirstName(updated.getFirstName());
            caregiver.setMiddleName(updated.getMiddleName());
            caregiver.setLastName(updated.getLastName());
            caregiver.setEmail(updated.getEmail());
            caregiver.setMobile_number(updated.getMobile_number());
            caregiver.setGender(updated.getGender());
            caregiver.setAddress(updated.getAddress());
            return repo.save(caregiver);
        }).orElse(null);
    }

    // In CaregiverService.java
    public CaregiverDto getDtoById(Long id) {
        Caregiver caregiver = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));

        List<PatientSummaryDto> patientDtos = caregiver.getPatients().stream()
                .map(p -> new PatientSummaryDto(p.getId(), p.getFirst_name(), p.getLastName()))
                .toList();

        return new CaregiverDto(
                caregiver.getId(),
                caregiver.getUsername(),
                caregiver.getEmail(),
                caregiver.getFirstName(),
                caregiver.getMiddleName(),
                caregiver.getLastName(),
                caregiver.getAddress(),
                patientDtos
        );
    }

    @Autowired
    private CaregiverRepository caregiverRepository;

    public String getPhoneNumber(Long caregiverId) {
        return caregiverRepository.findById(caregiverId)
                .map(Caregiver::getMobile_number)
                .orElse(null);
    }
}