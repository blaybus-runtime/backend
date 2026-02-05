package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.planner.dto.response.WorksheetUploadResponseDto;
import com.blaybus.backend.domain.planner.repository.WorksheetRepository;
import com.blaybus.backend.domain.user.MenteeProfile;
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
            // 필요하면 "image/jpg" 추가
    );

    // ✅ 멘토 업로드
    @Transactional
    public WorksheetUploadResponseDto uploadWorksheetAsMentor(
            MultipartFile file,
            String title,
            String subject,
            String materialType
    ) {
        User user = getCurrentUserOrThrow();
        ensureRole(user, Role.MENTOR);

        MentorProfile mentorRef = em.getReference(MentorProfile.class, user.getId());

        return uploadWorksheetInternal(file, title, subject, materialType, user, mentorRef, null);
    }

    // ✅ 멘티 업로드
    @Transactional
    public WorksheetUploadResponseDto uploadWorksheetAsMentee(
            MultipartFile file,
            String title,
            String subject,
            String materialType
    ) {
        User user = getCurrentUserOrThrow();
        ensureRole(user, Role.MENTEE);

        MenteeProfile menteeRef = em.getReference(MenteeProfile.class, user.getId());

        return uploadWorksheetInternal(file, title, subject, materialType, user, null, menteeRef);
    }

    // ======================
    // 공통 로직
    // ======================
    private WorksheetUploadResponseDto uploadWorksheetInternal(
            MultipartFile file,
            String title,
            String subject,
            String materialType,
            User user,
            MentorProfile mentorRef,
            MenteeProfile menteeRef
    ) {
        validate(file, title, subject);

        MaterialType mt = parseMaterialType(materialType);

        String savedName = saveToLocal(file);
        String fileUrl = "/files/worksheets/" + savedName;

        Worksheet worksheet = Worksheet.builder()
                .mentor(mentorRef) // 멘토 업로드면 set, 아니면 null
                .mentee(menteeRef) // 멘티 업로드면 set, 아니면 null
                .title(title)
                .subject(subject)
                .materialType(mt)
                .fileUrl(fileUrl)
                .build();

        Worksheet saved = worksheetRepository.save(worksheet);

        // ⚠️ DTO에 mentorId만 있으면 멘티 업로드에서도 mentorId에 userId가 들어감(임시)
        // 다음 단계에서 uploaderId/uploaderRole로 DTO 개선 추천
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

    private User getCurrentUserOrThrow() {
        String username = SecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));
    }

    private void ensureRole(User user, Role expected) {
        if (user.getRole() != expected) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, expected + " 권한이 필요합니다.");
        }
    }

    private void validate(MultipartFile file, String title, String subject) {
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
    }

    private MaterialType parseMaterialType(String materialType) {
        if (materialType == null || materialType.isBlank()) return MaterialType.FILE;

        try {
            return MaterialType.valueOf(materialType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 materialType 입니다: " + materialType);
        }
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
