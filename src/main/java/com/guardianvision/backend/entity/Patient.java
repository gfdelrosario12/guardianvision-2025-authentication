package com.guardianvision.backend.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String salt;
    private String first_name;
    private String middle_name; // optional
    private String last_name;
    private Integer age;
    private Double height; // in centimeters or your chosen unit
    private Double weight; // in kilograms or your chosen unit
    private String address;
    private String gender;
    private String mobile_number;
    private String role;
    private String EmergencyContactName;
    private String EmergencyContactDetails;
    private String EmergencyContactAddress;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = false)
    private Caregiver caregiver;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alerts> alerts;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Outages> outages;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }

    public String getMiddle_name() { return middle_name; }
    public void setMiddle_name(String middle_name) { this.middle_name = middle_name; }

    public String getLast_name() { return last_name; }
    public void setLast_name(String last_name) { this.last_name = last_name; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmergencyContactName() {
        return EmergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        EmergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactDetails() {
        return EmergencyContactDetails;
    }

    public void setEmergencyContactDetails(String emergencyContactDetails) {
        EmergencyContactDetails = emergencyContactDetails;
    }

    public String getEmergencyContactAddress() {
        return EmergencyContactAddress;
    }

    public void setEmergencyContactAddress(String emergencyContactAddress) {
        EmergencyContactAddress = emergencyContactAddress;
    }

    public Caregiver getCaregiver() {
        return caregiver;
    }

    public void setCaregiver(Caregiver caregiver) {
        this.caregiver = caregiver;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}