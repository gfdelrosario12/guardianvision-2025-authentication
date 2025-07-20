package com.guardianvision.backend.service;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SmsService {

    private final VonageClient vonageClient;
    private final String fromSenderName;

    // Pattern to extract a place name/address from Google Maps URLs (e.g., /place/Eiffel+Tower/)
    private static final Pattern GOOGLE_MAPS_PLACE_PATTERN = Pattern.compile("/place/([^/]+)/");

    // Pattern to extract latitude and longitude from Google Maps URLs (e.g., @latitude,longitude)
    private static final Pattern GOOGLE_MAPS_COORDINATE_PATTERN = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)(?:,\\d+z)?");

    public SmsService(
            @Value("${vonage.api-key}") String apiKey,
            @Value("${vonage.api-secret}") String apiSecret,
            @Value("${vonage.sender-name}") String fromSenderName
    ) {
        this.vonageClient = VonageClient.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
        this.fromSenderName = fromSenderName;
        System.out.println("✅ Vonage client initialized.");
    }

    public void sendAlert(String rawPhoneNumber, String location) {
        String phoneNumber = formatPhoneNumber(rawPhoneNumber);
        String messageText;
        String displayLocation = location; // Default to the original location string

        // Check if the provided 'location' string appears to be a Google Maps URL.
        if (location != null && (location.contains("google.com/maps") || location.contains("maps.app.goo.gl"))) {

            // First, try to extract a human-readable place name/address from the URL
            Matcher placeMatcher = GOOGLE_MAPS_PLACE_PATTERN.matcher(location);
            if (placeMatcher.find()) {
                String encodedPlaceName = placeMatcher.group(1);
                try {
                    // URL-decode the extracted place name (e.g., "Eiffel+Tower" becomes "Eiffel Tower")
                    displayLocation = URLDecoder.decode(encodedPlaceName.replace("+", " "), StandardCharsets.UTF_8);
                    System.out.println("✅ Extracted place name from URL: " + displayLocation);
                } catch (IllegalArgumentException e) {
                    System.err.println("❌ Failed to URL-decode place name: " + encodedPlaceName + " - " + e.getMessage());
                    // Fallback to original encoded string if decoding fails
                    displayLocation = encodedPlaceName.replace("+", " ");
                }
            } else {
                // If no place name is found, try to extract coordinates
                Matcher coordinateMatcher = GOOGLE_MAPS_COORDINATE_PATTERN.matcher(location);
                if (coordinateMatcher.find()) {
                    String latitude = coordinateMatcher.group(1);
                    String longitude = coordinateMatcher.group(2);
                    displayLocation = "coordinates: " + latitude + ", " + longitude;
                    System.out.println("✅ Extracted coordinates from URL: " + displayLocation);
                } else {
                    System.err.println("⚠️ Could not extract specific location details from Google Maps link: " + location);
                    // If neither place name nor coordinates can be parsed, send the original link as fallback.
                    displayLocation = location;
                }
            }
        }

        // Construct the final message text using the determined displayLocation.
        messageText = "🚨 Emergency Alert: Help requested at " + displayLocation;

        try {
            System.out.println("📨 Sending SMS to: " + phoneNumber);
            System.out.println("📨 Message content: " + messageText);

            TextMessage message = new TextMessage(
                    fromSenderName,
                    phoneNumber,
                    messageText
            );

            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);

            if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
                System.out.println("✅ SMS sent successfully.");
            } else {
                System.err.println("❌ SMS failed with error: " + response.getMessages().get(0).getErrorText());
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
        }
    }

    @Async
    public void sendAlertAsync(String phoneNumber, String location) {
        sendAlert(phoneNumber, location);
        System.out.println("📤 [Async] SMS send initiated to: " + phoneNumber);
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("+")) return phoneNumber;
        if (phoneNumber.startsWith("09")) {
            return "+63" + phoneNumber.substring(1); // Converts "09xxxxxxxxx" to "+639xxxxxxxxx"
        }
        if (phoneNumber.startsWith("63")) {
            return "+" + phoneNumber; // Converts "639xxxxxxxxx" to "+639xxxxxxxxx"
        }
        System.err.println("⚠️ Unrecognized phone format, sending as-is: " + phoneNumber);
        return phoneNumber;
    }
}
