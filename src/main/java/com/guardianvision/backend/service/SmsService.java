package com.guardianvision.backend.service;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private final VonageClient vonageClient;
    private final String fromSenderName;

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
        String messageText = "🚨 Emergency Alert: Help requested at " + location;

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
            return "+63" + phoneNumber.substring(1);
        }
        if (phoneNumber.startsWith("63")) {
            return "+" + phoneNumber;
        }
        System.err.println("⚠️ Unrecognized phone format, sending as-is: " + phoneNumber);
        return phoneNumber;
    }
}
