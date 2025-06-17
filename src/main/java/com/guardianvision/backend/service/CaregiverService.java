package com.guardianvision.backend.service;

import com.guardianvision.backend.entity.Caregiver;
import com.guardianvision.backend.repository.CaregiverRepository;
import com.guardianvision.backend.util.PasswordArgon2;
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
            caregiver.setMiddleName(updated.getMiddleName());
            caregiver.setLastName(updated.getLastName());
            caregiver.setAddress(updated.getAddress());
            caregiver.setGender(updated.getGender());
            caregiver.setMobile_number(updated.getMobile_number());
            return repo.save(caregiver);
        }).orElse(null);
    }


    public boolean login(String username, String rawPassword) {
        Caregiver caregiver = repo.findByUsername(username);
        if (caregiver == null) return false;
        PasswordArgon2 argon2 = new PasswordArgon2();
        return argon2.matchPasswords(rawPassword, caregiver.getSalt(), caregiver.getPassword());
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

}