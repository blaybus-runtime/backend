package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.planner.dto.response.WorksheetUploadResponseDto;
import com.blaybus.backend.domain.planner.repository.WorksheetRepository;
import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.global.enum_type.MaterialType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorksheetService {

    private final WorksheetRepository worksheetRepository;

    @PersistenceContext
    private EntityManager em;

    @Value("${app.upload.worksheets-dir:uploads/worksheets}")
    private String worksheetsDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    @Transactional
    public WorksheetUploadResponseDto uploadWorksheet(
            MultipartFile file,
            String title,
            String subject,
            String materialType,
            Long mentorId
    ) {
        // 1) 검증
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("unsupported file type: " + contentType);
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject is required");
        }

        // 2) materialType String -> Enum
        MaterialType mt = (materialType == null || materialType.isBlank())
                ? MaterialType.FILE
                : MaterialType.valueOf(materialType);

        // 3) 로컬 저장
        String savedName = saveToLocal(file);

        // 4) fileUrl 규칙 (WebConfig의 /files/worksheets/** 와 맞춰야 함)
        String fileUrl = "/files/worksheets/" + savedName;

        // 5) mentor 엔티티 참조(프록시) 가져오기 (DB 조회 없이 FK만 세팅 가능)
        MentorProfile mentorRef = em.getReference(MentorProfile.class, mentorId);

        // 6) Worksheet 생성/저장
        Worksheet worksheet = Worksheet.builder()
                .mentor(mentorRef)
                .title(title)
                .subject(subject)
                .materialType(mt)
                .fileUrl(fileUrl)
                .build();

        Worksheet saved = worksheetRepository.save(worksheet);

        // 7) 응답 DTO (mentorId는 mentor 엔티티에서 꺼내면 됨)
        return WorksheetUploadResponseDto.builder()
                .worksheetId(saved.getId())
                .title(saved.getTitle())
                .subject(saved.getSubject())
                .materialType(saved.getMaterialType().name())
                .fileUrl(saved.getFileUrl())
                .mentorId(saved.getMentor().getUserId())  // MentorProfile PK가 userId인 구조
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private String saveToLocal(MultipartFile file) {
        try {
            Path dir = Paths.get(worksheetsDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);

            String savedName = UUID.randomUUID() + ext;
            Path target = dir.resolve(savedName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return savedName;
        } catch (IOException e) {
            throw new RuntimeException("failed to save worksheet file", e);
        }
    }
}
