// CaregiverRepository.java
package com.guardianvision.backend.repository;

import com.guardianvision.backend.entity.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaregiverRepository extends JpaRepository<Caregiver, Long> {
    Caregiver findByUsername(String username);

    Long id(Long id);
}
