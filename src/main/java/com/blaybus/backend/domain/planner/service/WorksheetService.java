package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.planner.dto.response.WorksheetUploadResponseDto;
import com.blaybus.backend.domain.planner.repository.WorksheetRepository;
import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.MaterialType;
import com.blaybus.backend.global.enum_type.Role;
import com.blaybus.backend.global.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorksheetService {

    private final WorksheetRepository worksheetRepository;
    private final UserRepository userRepository;

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
            String materialType
            // Long mentorId <- 파라미터에서 제거 (토큰 정보 사용)
    ) {
        // 1. 토큰에서 유저 정보 가져오기
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        // 2. 권한 확인
        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토 권한이 필요합니다.");
        }

        // 3. 파일 및 입력값 검증
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일은 필수입니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일 형식입니다: " + contentType);
        }
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제목은 필수입니다.");
        }
        if (subject == null || subject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "과목은 필수입니다.");
        }

        // 4. 비즈니스 로직 (MaterialType 변환 및 로컬 저장)
        MaterialType mt = (materialType == null || materialType.isBlank())
                ? MaterialType.FILE
                : MaterialType.valueOf(materialType);

        String savedName = saveToLocal(file);
        String fileUrl = "/files/worksheets/" + savedName;

        // 5. MentorProfile 참조 가져오기 (user.getId() 사용)
        MentorProfile mentorRef = em.getReference(MentorProfile.class, user.getId());

        // 6. Worksheet 생성 및 저장
        Worksheet worksheet = Worksheet.builder()
                .mentor(mentorRef)
                .title(title)
                .subject(subject)
                .materialType(mt)
                .fileUrl(fileUrl)
                .build();

        Worksheet saved = worksheetRepository.save(worksheet);

        // 7. 응답 반환
        return WorksheetUploadResponseDto.builder()
                .worksheetId(saved.getId())
                .title(saved.getTitle())
                .subject(saved.getSubject())
                .materialType(saved.getMaterialType().name())
                .fileUrl(saved.getFileUrl())
                .mentorId(user.getId())
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
