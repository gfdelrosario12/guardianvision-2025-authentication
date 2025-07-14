package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Alerts;
import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.service.AlertService;
import com.guardianvision.backend.service.PatientService;
import com.guardianvision.backend.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/audio")
@EnableAsync
public class AudioAlertController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private AlertService alertService;

    @PostMapping("/alert")
    public ResponseEntity<?> triggerAlert(
            @RequestParam("patient_id") Long patientId,
            @RequestParam("location") String location,
            @RequestParam("timestamp") String timestampStr) {

        try {
            System.out.println("=== 🚨 [Manual Audio Alert Trigger] ===");
            System.out.println("📍 Location: " + location);
            System.out.println("⏱️ Timestamp: " + timestampStr);
            System.out.println("👤 Patient ID: " + patientId);

            Patient patient = patientService.getById(patientId);
            if (patient == null || patient.getCaregiver() == null) {
                System.err.println("❌ Patient or caregiver not found for ID: " + patientId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Patient or caregiver not found"));
            }

            Alerts alert = new Alerts();
            alert.setLastKnownLocation(location);
            alert.setTimestamp(timestampStr);

            // ✅ Offload to async method
            alertService.createAsync(patientId, alert);

            String phoneNumber = patient.getCaregiver().getMobile_number();
            if (phoneNumber != null) {
                // ✅ Offload to async method
                smsService.sendAlertAsync(phoneNumber, location);

                return ResponseEntity.ok(Map.of(
                        "status", "alert_sent",
                        "timestamp", timestampStr,
                        "location_saved", location
                ));
            } else {
                System.err.println("❌ Caregiver phone number not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Caregiver phone number not found"));
            }

        } catch (Exception e) {
            System.err.println("💥 Error occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
