package com.blaybus.backend.domain.content.service;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class R2StorageService {

    private final S3Client s3;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-base-url}")
    private String publicBaseUrl;


    public String upload(MultipartFile file, String objectKey) {
        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return objectKey;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "R2 업로드 실패");
        }
    }

    public ResponseInputStream<GetObjectResponse> download(String objectKey) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        return s3.getObject(req);
    }

    //생성 메서드
    public String uploadAndGetUrl(MultipartFile file, String objectKey) {
        upload(file, objectKey);
        return publicBaseUrl + "/" + objectKey;
    }

    //다운로드/삭제용
    public String extractKeyFromUrl(String fileUrl) {
        String prefix = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
        if (!fileUrl.startsWith(prefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileUrl이 public-base-url과 일치하지 않습니다.");
        }
        return fileUrl.substring(prefix.length());
    }

    //파일 삭제
    public void delete(String objectKey) {
        s3.deleteObject(b -> b.bucket(bucket).key(objectKey));
    }

}
