package com.guardianvision.backend.controller;

import com.guardianvision.backend.entity.Alerts;
import com.guardianvision.backend.entity.Patient;
import com.guardianvision.backend.service.AlertService;
import com.guardianvision.backend.service.PatientService;
import com.guardianvision.backend.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioTranscriptionController {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${whisper.api.url}")
    private String whisperApiUrl;

    @Autowired
    private PatientService patientService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private AlertService alertService;

    @PostMapping("/transcribe-and-alert")
    public ResponseEntity<?> transcribeAndAlert(
            @RequestParam("patient_id") Long patientId,
            @RequestParam("location") String location,
            @RequestParam("timestamp") String timestampStr,
            @RequestParam("file") MultipartFile file) {

        Path tempFile = null;

        try {
            System.out.println("=== 🎧 [Audio Transcription Request] ===");
            System.out.println("📍 Location: " + location);
            System.out.println("⏱️ Timestamp (raw): " + timestampStr);
            System.out.println("👤 Patient ID: " + patientId);

            tempFile = Files.createTempFile("audio", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            System.out.println("✅ Audio file saved to: " + tempFile);

            String transcript = sendToWhisperAPI(tempFile.toFile());
            System.out.println("📝 Transcript received: " + transcript);

            if (transcript.toLowerCase().contains("help")) {
                System.out.println("🔍 Keyword 'help' detected in transcript");

                Patient patient = patientService.getById(patientId);
                if (patient == null || patient.getCaregiver() == null) {
                    System.err.println("❌ Patient or caregiver not found for ID: " + patientId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Patient or caregiver not found"));
                }

                Alerts alert = new Alerts();
                alert.setLastKnownLocation(location);
                alert.setTimestamp(timestampStr); // store raw string here

                alertService.create(patientId, alert);
                System.out.println("📌 Alert saved to DB for patient " + patientId);

                String phoneNumber = patient.getCaregiver().getMobile_number();
                if (phoneNumber != null) {
                    System.out.println("📤 Sending alert SMS to: " + phoneNumber);
                    try {
                        smsService.sendAlert(phoneNumber, location);
                    } catch (Exception e) {
                        System.err.println("❌ SMS sending failed: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "SMS error: " + e.getMessage()));
                    }

                    return ResponseEntity.ok(Map.of(
                            "status", "alert_sent",
                            "transcript", transcript,
                            "timestamp", timestampStr,
                            "location_saved", location
                    ));
                } else {
                    System.err.println("❌ Caregiver phone number not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Caregiver phone number not found"));
                }
            } else {
                System.out.println("ℹ️ No alert keyword found in transcript");
                return ResponseEntity.ok(Map.of(
                        "status", "no_alert",
                        "transcript", transcript
                ));
            }

        } catch (Exception e) {
            System.err.println("💥 Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    System.out.println("🗑️ Temp file deleted: " + tempFile);
                } catch (IOException e) {
                    System.err.println("⚠️ Failed to delete temp file: " + e.getMessage());
                }
            }
        }
    }

    private String sendToWhisperAPI(File file) throws IOException {
        WebClient webClient = WebClient.create();

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(file));
        formData.add("model", "whisper-1");
        formData.add("language", "en");
        formData.add("response_format", "text");

        System.out.println("🚀 Sending file to Whisper API...");

        String response = webClient.post()
                .uri(whisperApiUrl)
                .header("Authorization", "Bearer " + openAiApiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("✅ Whisper API response received");
        return response != null ? response.trim() : "";
    }
}
