package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.content.service.R2StorageService;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorksheetService {

    private final WorksheetRepository worksheetRepository;
    private final UserRepository userRepository;
    private final R2StorageService r2StorageService;

    @PersistenceContext
    private EntityManager em;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
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

        // ✅ R2 objectKey 생성
        String original = (file.getOriginalFilename() == null) ? "file" : file.getOriginalFilename();
        original = sanitizeFilename(original);

        // 예: worksheets/mentor/12/uuid_original.pdf
        String objectKey = "worksheets/"
                + user.getRole().name().toLowerCase()
                + "/" + user.getId()
                + "/" + UUID.randomUUID() + "_" + original;

        // ✅ R2 업로드 + public URL 생성
        String fileUrl = r2StorageService.uploadAndGetUrl(file, objectKey);

        Worksheet worksheet = Worksheet.builder()
                .mentor(mentorRef)
                .mentee(menteeRef)
                .title(title)
                .subject(subject)
                .materialType(mt)
                .fileUrl(fileUrl)
                .build();

        Worksheet saved = worksheetRepository.save(worksheet);

        return WorksheetUploadResponseDto.builder()
                .worksheetId(saved.getId())
                .title(saved.getTitle())
                .subject(saved.getSubject())
                .materialType(saved.getMaterialType().name())
                .fileUrl(saved.getFileUrl())
                .uploaderId(user.getId())
                .uploaderRole(user.getRole().name())
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

    // ✅ objectKey 안전화 (경로 깨짐 방지)
    private String sanitizeFilename(String filename) {
        // 경로 문자 제거 + 너무 이상한 문자 치환
        String cleaned = filename.replace("\\", "_").replace("/", "_");
        cleaned = cleaned.replaceAll("[\\r\\n\\t]", "_");
        return cleaned;
    }
}
