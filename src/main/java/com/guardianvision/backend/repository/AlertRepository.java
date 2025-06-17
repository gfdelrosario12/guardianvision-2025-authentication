// AlertRepository.java
package com.guardianvision.backend.repository;

import com.guardianvision.backend.entity.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alerts, Long> {
    List<Alerts> findByPatientId(Long patientId);
}
