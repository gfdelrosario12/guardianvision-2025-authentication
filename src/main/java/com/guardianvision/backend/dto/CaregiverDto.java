// src/main/java/com/guardianvision/backend/dto/CaregiverDto.java
package com.guardianvision.backend.dto;

import java.util.List;

public class CaregiverDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String address;
    private List<PatientSummaryDto> patients;

    public CaregiverDto(Long id, String username, String email, String firstName, String middleName, String lastName, String address, List<PatientSummaryDto> patients) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.address = address;
        this.patients = patients;
    }

    // Getters...
}
