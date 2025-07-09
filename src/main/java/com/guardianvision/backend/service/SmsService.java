package com.guardianvision.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

@Service
public class SmsService {

    private final SnsClient snsClient;

    public SmsService(
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey,
            @Value("${aws.region}") String region) {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public void sendAlert(String phoneNumber, String location) {
        String message = "🚨 Emergency Alert: Help requested at " + location;

        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .build();

            PublishResponse result = snsClient.publish(request);
            System.out.println("✅ SMS sent! MessageId: " + result.messageId());

        } catch (SnsException e) {
            System.err.println("❌ Failed to send SMS: " + e.awsErrorDetails().errorMessage());
            throw e;
        }
    }
}
