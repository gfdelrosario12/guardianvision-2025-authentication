// src/main/java/com/guardianvision/backend/dto/PatientSummaryDto.java
package com.guardianvision.backend.dto;

public class PatientSummaryDto {
    private Long id;
    private String firstName;
    private String lastName;

    public PatientSummaryDto(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and setters (or use Lombok if you prefer)
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
