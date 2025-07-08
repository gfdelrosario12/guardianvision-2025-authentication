package com.guardianvision.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.*;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3UploadService {

    private final S3Presigner presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3UploadService(S3Client s3Client) {
        this.presigner = S3Presigner.builder()
                .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                .region(s3Client.serviceClientConfiguration().region())
                .build();
    }

    public UploadResponse generatePresignedUrl(String originalFilename, String contentType) throws URISyntaxException {
        String key = "patientpfps/" + UUID.randomUUID() + "-" + originalFilename;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        return new UploadResponse(presignedRequest.url().toURI(), key);
    }

    public record UploadResponse(URI url, String key) {}
}
