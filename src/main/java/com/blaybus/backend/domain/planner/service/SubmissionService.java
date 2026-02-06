package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.planner.Submission;
import com.blaybus.backend.domain.planner.SubmissionFile;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.SubmissionFileResponseDto;
import com.blaybus.backend.domain.planner.dto.response.SubmissionUploadResponseDto;
import com.blaybus.backend.domain.planner.repository.SubmissionFileRepository;
import com.blaybus.backend.domain.planner.repository.SubmissionRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
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

    @Value("${app.upload.submissions-dir:uploads/submissions}")
    private String submissionsDir;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 taskId 입니다. taskId=" + taskId));

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

        List<SubmissionFile> savedFiles = files.stream().map(file -> {
            String fileName = safeOriginalFilename(file);
            String fileUrl = uploadAndGetUrl(file);

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
     * WorksheetService와 동일한 방식: 로컬에 저장 후 /files/submissions/{filename} 반환
     */
    private String uploadAndGetUrl(MultipartFile file) {
        validateFile(file);
        String savedName = saveToLocal(file);
        return "/files/submissions/" + savedName;
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

    private String saveToLocal(MultipartFile file) {
        try {
            Path dir = Paths.get(submissionsDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = safeOriginalFilename(file);
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);

            String savedName = UUID.randomUUID() + ext;
            Path target = dir.resolve(savedName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return savedName;
        } catch (IOException e) {
            throw new RuntimeException("failed to save submission file", e);
        }
    }

    private String safeOriginalFilename(MultipartFile file) {
        String original = file.getOriginalFilename();
        return (original == null || original.isBlank()) ? "file" : original;
    }
}
