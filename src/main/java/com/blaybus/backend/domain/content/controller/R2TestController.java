package com.blaybus.backend.domain.content.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.blaybus.backend.domain.content.service.R2StorageService;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/r2-test")
public class R2TestController {

    private final R2StorageService r2StorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file) {
        String key = "test/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        r2StorageService.upload(file, key);
        return ResponseEntity.ok(key);
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam String key) {
        ResponseInputStream<GetObjectResponse> stream = r2StorageService.download(key);

        String filename = key.substring(key.lastIndexOf('/') + 1);
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }
}
