package com.guardianvision.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class S3UploadService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3UploadService(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public PresignedS3Upload generatePresignedUrl(String filename, String contentType) {
        String key;

        if (contentType.startsWith("image/")) {
            key = "patientpfps/" + filename;
        } else if (contentType.startsWith("video/")) {
            key = "recordings/" + filename;
        } else {
            throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return new PresignedS3Upload(presignedRequest.url(), key);
    }

    public record PresignedS3Upload(URL url, String key) {}
}
