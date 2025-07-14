package com.guardianvision.backend.controller;

import com.guardianvision.backend.service.S3UploadService;
import com.guardianvision.backend.service.S3UploadService.PresignedS3Upload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/s3")
public class S3UploadController {

    private final S3UploadService s3UploadService;

    public S3UploadController(S3UploadService s3UploadService) {
        this.s3UploadService = s3UploadService;
    }

    @PostMapping("/presign")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestBody Map<String, String> body) {
        String fileName = body.get("fileName");
        String fileType = body.get("fileType");

        PresignedS3Upload result = s3UploadService.generatePresignedUrl(fileName, fileType);

        return ResponseEntity.ok(Map.of(
                "url", result.url().toString(),
                "key", result.key()
        ));
    }
}
