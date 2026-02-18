package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.content.service.R2StorageService;
import com.blaybus.backend.domain.notification.dto.event.NotificationEvent;
import com.blaybus.backend.domain.planner.Submission;
import com.blaybus.backend.domain.planner.SubmissionFile;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.SubmissionFileResponseDto;
import com.blaybus.backend.domain.planner.dto.response.SubmissionUploadResponseDto;
import com.blaybus.backend.domain.planner.repository.SubmissionFileRepository;
import com.blaybus.backend.domain.planner.repository.SubmissionRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private final TodoRepository todoRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final R2StorageService r2StorageService;
    private final UserRepository userRepository; // [추가] 멘티 이름 조회용
    private final ApplicationEventPublisher eventPublisher; // [추가] 알림 발송용

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    /**
     * ✅ 최초 제출 / 추가 제출
     */
    public SubmissionUploadResponseDto submitFiles(Long menteeId, Long taskId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files는 최소 1개 이상 필요합니다.");
        }

        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 taskId 입니다. taskId=" + taskId));

        Submission submission = submissionRepository.findByTask_IdAndMenteeId(taskId, menteeId)
                .orElseGet(() -> submissionRepository.save(
                        Submission.builder()
                                .task(task)
                                .menteeId(menteeId)
                                .build()
                ));

        submission.touchSubmittedAt();

        Long submissionId = submission.getId();

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

        // ==========================================
        // ▼ [추가] 알림 발송 로직 (Merge: 내 로직 살림)
        // ==========================================
        sendSubmissionNotification(menteeId, task);
        // ==========================================

        // (Merge: 리턴은 서버 최신 반영본인 buildResponse 사용)
        return buildResponse(submission, menteeId, taskId, savedFiles);
    }

    /**
     * ✅ 제출 첨부 수정 (최종 상태 동기화)
     * (Merge: 서버에서 새로 추가된 메서드 유지)
     */
    public SubmissionUploadResponseDto updateSubmissionFiles(
            Long menteeId,
            Long taskId,
            String keepFileIdsRaw,
            List<MultipartFile> newFiles
    ) {
        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 taskId 입니다. taskId=" + taskId));

        Submission submission = submissionRepository.findByTask_IdAndMenteeId(taskId, menteeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "제출(submission)이 존재하지 않습니다. taskId=" + taskId));

        submission.touchSubmittedAt();

        Set<Long> keepIds = parseKeepFileIds(keepFileIdsRaw);

        // 🔥 삭제 대상 추출 (ConcurrentModification 방지)
        List<SubmissionFile> deleteTargets = submission.getFiles().stream()
                .filter(sf -> !keepIds.contains(sf.getId()))
                .collect(Collectors.toList());

        // 1️⃣ 기존 파일 삭제
        for (SubmissionFile sf : deleteTargets) {
            try {
                String objectKey = r2StorageService.extractKeyFromUrl(sf.getFileUrl());
                r2StorageService.delete(objectKey);
            } catch (Exception ignored) {
                // best-effort
            }

            submission.getFiles().remove(sf);
            submissionFileRepository.delete(sf);
        }

        // 2️⃣ 새 파일 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            Long submissionId = submission.getId();

            for (MultipartFile file : newFiles) {
                if (file == null || file.isEmpty()) continue;

                String fileName = safeOriginalFilename(file);
                String fileUrl = uploadAndGetUrlToR2(file, menteeId, taskId, submissionId);

                SubmissionFile sf = SubmissionFile.builder()
                        .submission(submission)
                        .fileName(fileName)
                        .fileUrl(fileUrl)
                        .build();

                SubmissionFile saved = submissionFileRepository.save(sf);
                submission.addFile(saved);
            }
        }

        // (선택 사항) 수정 시에도 알림을 보내고 싶다면 여기에 sendSubmissionNotification(menteeId, task); 추가

        // ✅ 최종 상태 반환
        return buildResponse(submission, menteeId, taskId, submission.getFiles());
    }

    /* =========================
       🔧 내부 유틸 메서드
       ========================= */

    private Set<Long> parseKeepFileIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptySet();
        }

        String s = raw.trim();

        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1).trim();
        }

        if (s.isBlank()) {
            return Collections.emptySet();
        }

        try {
            Set<Long> result = new HashSet<>();
            for (String token : s.split(",")) {
                String t = token.trim();
                if (!t.isEmpty()) {
                    result.add(Long.parseLong(t));
                }
            }
            return result;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "keepFileIds 형식이 올바르지 않습니다. 예: 1,2,3"
            );
        }
    }

    private SubmissionUploadResponseDto buildResponse(
            Submission submission,
            Long menteeId,
            Long taskId,
            List<SubmissionFile> files
    ) {
        List<SubmissionFileResponseDto> fileDtos = files.stream()
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
     * [추가] 과제 제출 알림 발송 헬퍼 메서드 (Merge: 내 로직 살림)
     */
    private void sendSubmissionNotification(Long menteeId, TodoTask task) {
        User mentee = userRepository.findById(menteeId).orElse(null);
        if (mentee == null) return;

        User mentorToNotify = findMentorForTask(task);

        if (mentorToNotify != null) {
            eventPublisher.publishEvent(new NotificationEvent(
                    mentorToNotify,
                    mentee.getName() + " 학생이 과제를 제출했습니다.",
                    "/tasks/" + task.getId(),
                    NotificationType.SUBMISSION
            ));
        }
    }

    /**
     * [추가] Task와 연결된 멘토를 찾는 로직 (Merge: 내 로직 살림)
     */
    private User findMentorForTask(TodoTask task) {
        if (task.getWorksheet() != null && task.getWorksheet().getMentor() != null) {
            return task.getWorksheet().getMentor().getUser();
        }

        if (task.getTaskWorksheets() != null && !task.getTaskWorksheets().isEmpty()) {
            Worksheet firstWorksheet = task.getTaskWorksheets().get(0).getWorksheet();
            if (firstWorksheet != null && firstWorksheet.getMentor() != null) {
                return firstWorksheet.getMentor().getUser();
            }
        }
        return null;
    }

    /**
     * ✅ R2에 업로드하고 public URL 반환
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
        String cleaned = filename.replace("\\", "_").replace("/", "_");
        cleaned = cleaned.replaceAll("[\\r\\n\\t]", "_");
        return cleaned;
    }
}