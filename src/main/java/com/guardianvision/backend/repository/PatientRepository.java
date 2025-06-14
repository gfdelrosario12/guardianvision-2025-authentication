// PatientRepository.java
package com.guardianvision.backend.repository;

import com.guardianvision.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByCaregiverId(Long caregiverId);
    Patient findByUsername(String username);
}
