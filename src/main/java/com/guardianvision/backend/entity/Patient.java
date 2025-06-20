package com.guardianvision.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;
    private Integer age;
    private Double height; // in centimeters or your chosen unit
    private Double weight; // in kilograms or your chosen unit
    private String address;
    private String gender;
    private String mobile_number;
    private String role;
    @JsonProperty("emergency_contact_name")
    private String emergency_contact_name;

    @JsonProperty("emergency_contact_number")
    private String emergency_contact_number;

    @JsonProperty("emergency_contact_address")
    private String emergency_contact_address;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caregiver_id")
    @JsonIgnoreProperties({"patients"}) // This avoids circular reference
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

    public String getFirst_name() { return firstName; }
    public void setFirst_name(String first_name) { this.firstName = first_name; }

    public String getMiddle_name() { return middleName; }
    public void setMiddle_name(String middle_name) { this.middleName = middle_name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

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
        return emergency_contact_name;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        emergency_contact_name = emergencyContactName;
    }

    public String getEmergencyContactDetails() {
        return emergency_contact_number;
    }

    public void setEmergencyContactDetails(String emergencyContactDetails) {
        emergency_contact_number = emergencyContactDetails;
    }

    public String getEmergencyContactAddress() {
        return emergency_contact_address;
    }

    public void setEmergencyContactAddress(String emergencyContactAddress) {
        emergency_contact_address = emergencyContactAddress;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}