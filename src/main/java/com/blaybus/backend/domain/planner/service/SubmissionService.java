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
    private final UserRepository userRepository; // [추가] 멘티 이름 조회용
    private final ApplicationEventPublisher eventPublisher; // [추가] 알림 발송용

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

        // ==========================================
        // ▼ [추가] 알림 발송 로직
        // ==========================================
        sendSubmissionNotification(menteeId, task);
        // ==========================================

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
     * [추가] 과제 제출 알림 발송 헬퍼 메서드
     */
    private void sendSubmissionNotification(Long menteeId, TodoTask task) {
        // 1. 멘티 정보 조회 (이름 표시용)
        User mentee = userRepository.findById(menteeId).orElse(null);
        if (mentee == null) return;

        // 2. 알림 받을 멘토 찾기
        User mentorToNotify = findMentorForTask(task);

        // 3. 알림 발송
        if (mentorToNotify != null) {
            eventPublisher.publishEvent(new NotificationEvent(
                    mentorToNotify,
                    mentee.getName() + " 학생이 과제를 제출했습니다.", // 예: "홍길동 학생이 과제를 제출했습니다."
                    "/tasks/" + task.getId(), // 멘토가 확인할 페이지 URL
                    NotificationType.SUBMISSION
            ));
        }
    }

    /**
     * [추가] Task와 연결된 멘토를 찾는 로직 (구조 변경 대응)
     */
    private User findMentorForTask(TodoTask task) {
        // 우선순위 1: 기존 worksheet 필드 (Legacy)
        if (task.getWorksheet() != null && task.getWorksheet().getMentor() != null) {
            return task.getWorksheet().getMentor().getUser();
        }

        // 우선순위 2: 새로운 taskWorksheets (N:M) 리스트 확인
        if (task.getTaskWorksheets() != null && !task.getTaskWorksheets().isEmpty()) {
            // 연결된 첫 번째 학습지의 멘토에게 알림 (보통 과제는 한 멘토가 내주므로)
            Worksheet firstWorksheet = task.getTaskWorksheets().get(0).getWorksheet();
            if (firstWorksheet != null && firstWorksheet.getMentor() != null) {
                return firstWorksheet.getMentor().getUser();
            }
        }

        // 멘토가 없는 자습용 Task라면 알림 안 보냄
        return null;
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
