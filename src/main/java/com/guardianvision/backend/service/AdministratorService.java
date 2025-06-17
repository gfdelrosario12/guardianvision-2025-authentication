package com.guardianvision.backend.service;

import com.guardianvision.backend.entity.Administrator;
import com.guardianvision.backend.repository.AdministratorRepository;
import com.guardianvision.backend.util.PasswordArgon2;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministratorService {
    private final AdministratorRepository repo;

    public AdministratorService(AdministratorRepository repo) {
        this.repo = repo;
    }

    public List<Administrator> getAll() {
        return repo.findAll();
    }

    public Administrator getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Administrator create(Administrator admin) {
        return repo.save(admin);
    }

    public Administrator update(Long id, Administrator updated) {
        return repo.findById(id).map(admin -> {
            admin.setEmail(updated.getEmail());

            admin.setFirstName(updated.getFirstName());
            admin.setMiddleName(updated.getMiddleName()); // Optional
            admin.setLastName(updated.getLastName());

            admin.setAddress(updated.getAddress());
            admin.setGender(updated.getGender());
            admin.setMobile_number(updated.getMobile_number());

            return repo.save(admin);
        }).orElse(null);
    }

    public boolean login(String username, String rawPassword) {
        Administrator admin = repo.findByUsername(username);
        if (admin == null) return false;
        PasswordArgon2 argon2 = new PasswordArgon2();
        return argon2.matchPasswords(admin.getSalt(), rawPassword, admin.getPassword());
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Administrator changePassword(Long id, String newPassword) {
        return repo.findById(id).map(admin -> {
            String newSalt = PasswordArgon2.generateSalt();
            String newHash = PasswordArgon2.encryptPassword(newPassword, newSalt);
            admin.setSalt(newSalt);
            admin.setPassword(newHash);
            return repo.save(admin);
        }).orElse(null);
    }

    public Long getLastInsertedId() {
        List<Administrator> entities = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return entities.isEmpty() ? 1L : entities.get(0).getId() + 1;
    }

    public String username(Long lastID) {
        return "GV - AA - " + lastID;
    }
}
