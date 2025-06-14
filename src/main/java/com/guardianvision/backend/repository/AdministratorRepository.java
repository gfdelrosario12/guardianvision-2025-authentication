// AdministratorRepository.java
package com.guardianvision.backend.repository;

import com.guardianvision.backend.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Administrator findByUsername(String username);
}
