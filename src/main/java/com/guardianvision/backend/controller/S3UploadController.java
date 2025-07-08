package com.guardianvision.backend.controller;

import com.guardianvision.backend.service.S3UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.Map;

@RestController
@RequestMapping("/api/s3")
public class S3UploadController {

    private final S3UploadService s3UploadService;

    public S3UploadController(S3UploadService s3UploadService) {
        this.s3UploadService = s3UploadService;
    }

    @PostMapping("/presign")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestBody Map<String, String> body) throws URISyntaxException {
        String fileName = body.get("fileName");
        String fileType = body.get("fileType");

        var result = s3UploadService.generatePresignedUrl(fileName, fileType);

        return ResponseEntity.ok(Map.of(
                "url", result.url().toString(),
                "key", result.key()
        ));
    }
}
