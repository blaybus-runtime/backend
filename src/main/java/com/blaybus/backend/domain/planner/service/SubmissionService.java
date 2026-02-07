package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.service.R2StorageService;
import com.blaybus.backend.domain.planner.Submission;
import com.blaybus.backend.domain.planner.SubmissionFile;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.SubmissionFileResponseDto;
import com.blaybus.backend.domain.planner.dto.response.SubmissionUploadResponseDto;
import com.blaybus.backend.domain.planner.repository.SubmissionFileRepository;
import com.blaybus.backend.domain.planner.repository.SubmissionRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private final TodoRepository todoRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final R2StorageService r2StorageService;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    public SubmissionUploadResponseDto submitFiles(Long menteeId, Long taskId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files는 최소 1개 이상 필요합니다.");
        }

        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 taskId 입니다. taskId=" + taskId));

        // ✅ 1 task 당 1 submission (없으면 생성 / 있으면 그 submission에 파일 추가)
        Submission submission = submissionRepository.findByTask_IdAndMenteeId(taskId, menteeId)
                .orElseGet(() -> submissionRepository.save(
                        Submission.builder()
                                .task(task)
                                .menteeId(menteeId)
                                .build()
                ));

        // 재제출이라면 시간 갱신
        submission.touchSubmittedAt();

        Long submissionId = submission.getId(); // 이미 save 되었거나 기존이라 존재함

        List<SubmissionFile> savedFiles = files.stream().map(file -> {
            String fileName = safeOriginalFilename(file);
            String fileUrl = uploadAndGetUrlToR2(file, menteeId, taskId, submissionId);

            SubmissionFile sf = SubmissionFile.builder()
                    .submission(submission)
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .build();

            SubmissionFile saved = submissionFileRepository.save(sf);
            submission.addFile(saved);
            return saved;
        }).collect(Collectors.toList());

        List<SubmissionFileResponseDto> fileDtos = savedFiles.stream()
                .map(sf -> SubmissionFileResponseDto.builder()
                        .fileId(sf.getId())
                        .fileName(sf.getFileName())
                        .fileUrl(sf.getFileUrl())
                        .build())
                .collect(Collectors.toList());

        return SubmissionUploadResponseDto.builder()
                .assignmentId(submission.getId())
                .menteeId(menteeId)
                .taskId(taskId)
                .status(submission.getStatus().name())
                .submittedAt(submission.getSubmittedAt())
                .files(fileDtos)
                .build();
    }

    /**
     * ✅ R2에 업로드하고 public URL 반환
     * objectKey 예:
     * submissions/task-10/mentee-3/submission-55/uuid_original.pdf
     */
    private String uploadAndGetUrlToR2(MultipartFile file, Long menteeId, Long taskId, Long submissionId) {
        validateFile(file);

        String original = sanitizeFilename(safeOriginalFilename(file));
        String objectKey = "submissions/"
                + "task-" + taskId
                + "/mentee-" + menteeId
                + "/submission-" + submissionId
                + "/" + UUID.randomUUID() + "_" + original;

        return r2StorageService.uploadAndGetUrl(file, objectKey);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일은 필수입니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "지원하지 않는 파일 형식입니다: " + contentType
            );
        }
    }

    private String safeOriginalFilename(MultipartFile file) {
        String original = file.getOriginalFilename();
        return (original == null || original.isBlank()) ? "file" : original;
    }

    private String sanitizeFilename(String filename) {
        // 경로 문자 제거 + 줄바꿈 제거
        String cleaned = filename.replace("\\", "_").replace("/", "_");
        cleaned = cleaned.replaceAll("[\\r\\n\\t]", "_");
        return cleaned;
    }
}
