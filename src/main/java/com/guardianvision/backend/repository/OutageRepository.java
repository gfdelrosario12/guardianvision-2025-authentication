// OutageRepository.java
package com.guardianvision.backend.repository;

import com.guardianvision.backend.entity.Outages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutageRepository extends JpaRepository<Outages, Long> {
    List<Outages> findByPatientId(Long patientId);
}
