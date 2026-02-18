package com.blaybus.backend.domain.content.service;

import com.blaybus.backend.domain.content.Feedback;
import com.blaybus.backend.domain.content.dto.request.FeedbackRequest;
import com.blaybus.backend.domain.content.dto.response.FeedbackResponse;
import com.blaybus.backend.domain.content.repository.FeedbackFileRepository;
import com.blaybus.backend.domain.content.repository.FeedbackRepository;
import com.blaybus.backend.domain.notification.dto.event.NotificationEvent;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.domain.user.MentorProfile;
import com.blaybus.backend.domain.user.User;
import com.blaybus.backend.domain.user.repository.MentorProfileRepository;
import com.blaybus.backend.domain.user.repository.UserRepository;
import com.blaybus.backend.global.enum_type.NotificationType;
import com.blaybus.backend.global.enum_type.Role;
import com.blaybus.backend.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.blaybus.backend.domain.content.FeedbackFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TodoRepository todoRepository;

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;

    private final FeedbackFileRepository feedbackFileRepository;
    private final R2StorageService r2StorageService;

    // ▼ [추가] 알림 이벤트를 발행하기 위한 도구
    private final ApplicationEventPublisher eventPublisher;

    //피드백 작성
//    @Transactional
//    public FeedbackResponse.Create createMentorFeedback(Long taskId, FeedbackRequest.Create request) {
//
//        String username = SecurityUtils.getCurrentUsername();
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));
//
//        if (user.getRole() != Role.MENTOR) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 피드백을 작성할 수 있습니다.");
//        }
//
//        MentorProfile mentor = mentorProfileRepository.findById(user.getId())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멘토 프로필을 찾을 수 없습니다."));
//
//        // 과제(task) 조회 (기존 assignmentId → taskId)
//        TodoTask task = todoRepository.findById(taskId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));
//
//        // 중복 작성 방지 (task당 1개)
//        Feedback feedback = feedbackRepository.findByTask_Id(taskId).orElse(null);
//
//        if (feedback == null) {
//            feedback = feedbackRepository.save(
//                    Feedback.builder()
//                            .task(task)
//                            .mentor(mentor)
//                            .content(request.content())
//                            .build()
//            );
//        } else {
//            if (!feedback.getMentor().getUserId().equals(user.getId())) {
//                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 피드백만 다시 작성할 수 있습니다.");
//            }
//
//            if (feedback.getContent() != null) {
//                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 피드백이 작성된 과제입니다.");
//            }
//            feedback.updateContent(request.content());
//        }
//
//        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
//                mentor.getUserId(),
//                user.getName(),
//                user.getProfileImage()
//        );
//
//        return new FeedbackResponse.Create(
//                feedback.getId(),
//                taskId,
//                mentorInfo,
//                feedback.getContent(),
//                feedback.getCreatedAt(),
//                List.of()
//        );
//    }

    //피드백 조회
    @Transactional(readOnly = true)
    public FeedbackResponse.Create getFeedback(Long taskId) {

        Feedback feedback = feedbackRepository.findByTask_Id(taskId).orElse(null);
        if (feedback == null || feedback.getContent() == null) {
            return null;
        }

        User mentorUser = feedback.getMentor().getUser();

        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                feedback.getMentor().getUserId(),
                mentorUser.getName(),
                mentorUser.getProfileImage()
        );

        var fileInfos = feedbackFileRepository.findAllByFeedback_Id(feedback.getId()).stream()
                .map(f -> new FeedbackResponse.FileInfo(f.getId(), f.getFileName(), f.getFileUrl()))
                .toList();

        return new FeedbackResponse.Create(
                feedback.getId(),
                taskId,
                mentorInfo,
                feedback.getContent(),
                feedback.getCreatedAt(),
                fileInfos
        );

    }

    //피드백 수정
    @Transactional
    public FeedbackResponse.Create updateMentorFeedback(
            Long taskId,
            FeedbackRequest.Create request
    ) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."
                ));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "멘토만 피드백을 수정할 수 있습니다."
            );
        }

        Feedback feedback = feedbackRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "수정할 피드백이 없습니다."
                ));

        if (feedback.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 피드백이 없습니다.");
        }

        if (!feedback.getMentor().getUserId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인이 작성한 피드백만 수정할 수 있습니다."
            );
        }

        feedback.updateContent(request.content());

        // ▼ [추가] 여기서 알림 발송! (isUpdate = true)
        // task 정보는 feedback.getTask()로 가져옵니다.
        sendFeedbackNotification(feedback.getTask(), user, true);

        User mentorUser = feedback.getMentor().getUser();
        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                mentorUser.getId(),
                mentorUser.getName(),
                mentorUser.getProfileImage()
        );

        var fileInfos = feedbackFileRepository.findAllByFeedback_Id(feedback.getId()).stream()
                .map(f -> new FeedbackResponse.FileInfo(f.getId(), f.getFileName(), f.getFileUrl()))
                .toList();

        return new FeedbackResponse.Create(
                feedback.getId(),
                taskId,
                mentorInfo,
                feedback.getContent(),
                feedback.getCreatedAt(),
                fileInfos
        );
    }

    //피드백 삭제
    @Transactional
    public void deleteMentorFeedback(Long taskId) {

        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."
                ));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "멘토만 피드백을 삭제할 수 있습니다."
            );
        }

        Feedback feedback = feedbackRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "삭제할 피드백이 없습니다."
                ));

        if (!feedback.getMentor().getUserId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "본인이 작성한 피드백만 삭제할 수 있습니다."
            );
        }

        // 1) 파일 전부 삭제 (R2 + DB)
        List<FeedbackFile> files = feedbackFileRepository.findAllByFeedback_Id(feedback.getId());

        for (FeedbackFile file : files) {
            // R2 삭제 (실패하면 전체 롤백이 싫다면 try/catch 정책 선택)
            String objectKey = r2StorageService.extractKeyFromUrl(file.getFileUrl());
            r2StorageService.delete(objectKey);
        }
        feedbackFileRepository.deleteAll(files);

        // 2) content soft delete
        feedback.clearContent();
    }



    //파일 업로드
    @Transactional
    public List<FeedbackResponse.FileInfo> uploadFeedbackFiles(Long taskId, List<MultipartFile> files) {

        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 파일을 업로드할 수 있습니다.");
        }

        Feedback feedback = feedbackRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백이 없습니다."));

        // 작성자 검증
        if (!feedback.getMentor().getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 피드백에만 파일 업로드 가능합니다.");
        }

        // content가 null(삭제됨) 상태면 업로드 막기
        if (feedback.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "삭제된 피드백입니다. 먼저 피드백을 작성하세요.");
        }

        var saved = new ArrayList<FeedbackFile>();

        for (MultipartFile file : files) {

            //file.getOriginalFilename()이 null일때
            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) {
                original = "file";
            }

            String key = "feedback/" + taskId + "/" + UUID.randomUUID() + "_" + original;
            String url = r2StorageService.uploadAndGetUrl(file, key);

            saved.add(feedbackFileRepository.save(
                    FeedbackFile.builder()
                            .feedback(feedback)
                            .fileName(original)
                            .fileUrl(url)
                            .build()
            ));
        }

        return saved.stream()
                .map(f -> new FeedbackResponse.FileInfo(f.getId(), f.getFileName(), f.getFileUrl()))
                .toList();
    }


    //파일 다운로드
    @Transactional(readOnly = true)
    public ResponseEntity<InputStreamResource> downloadFeedbackFile(Long taskId, Long fileId) {

        Feedback feedback = feedbackRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백이 없습니다."));

        FeedbackFile file = feedbackFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."));

        if (!file.getFeedback().getId().equals(feedback.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 피드백의 파일이 아닙니다.");
        }

        //삭제된 피드백일때
        if (feedback.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제된 피드백입니다.");
        }

        String objectKey = r2StorageService.extractKeyFromUrl(file.getFileUrl());
        var stream = r2StorageService.download(objectKey);

        String encoded = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }


    //파일 삭제
    @Transactional
    public void deleteFeedbackFile(Long taskId, Long fileId) {

        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 파일을 삭제할 수 있습니다.");
        }

        Feedback feedback = feedbackRepository.findByTask_Id(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "피드백이 없습니다."));

        if (!feedback.getMentor().getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 피드백의 파일만 삭제할 수 있습니다.");
        }

        //피드백 삭제면 종료 취급
        if (feedback.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제된 피드백입니다.");
        }

        FeedbackFile file = feedbackFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."));

        if (!file.getFeedback().getId().equals(feedback.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 피드백의 파일이 아닙니다.");
        }

        // (선택) R2에서도 삭제
        String objectKey = r2StorageService.extractKeyFromUrl(file.getFileUrl());
        r2StorageService.delete(objectKey);

        feedbackFileRepository.delete(file);
    }

    //피드백+파일로 수정
    @Transactional
    public FeedbackResponse.Create createMentorFeedbackWithFiles(Long taskId, String content, List<MultipartFile> files) {

        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content는 필수입니다.");
        }

        // 1) 기존 createMentorFeedback 로직 그대로 (request.content() 대신 content 사용)
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유저를 찾을 수 없습니다."));

        if (user.getRole() != Role.MENTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "멘토만 피드백을 작성할 수 있습니다.");
        }

        MentorProfile mentor = mentorProfileRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "멘토 프로필을 찾을 수 없습니다."));

        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "과제를 찾을 수 없습니다."));

        Feedback feedback = feedbackRepository.findByTask_Id(taskId).orElse(null);

        if (feedback == null) {
            feedback = feedbackRepository.save(
                    Feedback.builder()
                            .task(task)
                            .mentor(mentor)
                            .content(content)
                            .build()
            );
        } else {
            if (!feedback.getMentor().getUserId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 피드백만 다시 작성할 수 있습니다.");
            }
            if (feedback.getContent() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 피드백이 작성된 과제입니다.");
            }
            feedback.updateContent(content);
        }

        // 2) 파일 있으면 업로드 (너 기존 로직 그대로)
        if (files != null && !files.isEmpty()) {
            uploadFeedbackFiles(taskId, files);
        }

        FeedbackResponse.MentorInfo mentorInfo = new FeedbackResponse.MentorInfo(
                mentor.getUserId(),
                user.getName(),
                user.getProfileImage()
        );

        var fileInfos = feedbackFileRepository.findAllByFeedback_Id(feedback.getId()).stream()
                .map(f -> new FeedbackResponse.FileInfo(f.getId(), f.getFileName(), f.getFileUrl()))
                .toList();

        // ▼ [추가] 여기서 알림 발송! (isUpdate = false)
        sendFeedbackNotification(task, user, false);

        return new FeedbackResponse.Create(
                feedback.getId(),
                taskId,
                mentorInfo,
                feedback.getContent(),
                feedback.getCreatedAt(),
                fileInfos
        );
    }


    /**
     * [추가] 피드백 알림 발송 헬퍼 메서드
     */
    private void sendFeedbackNotification(TodoTask task, User mentor, boolean isUpdate) {
        // 1. 알림 받을 멘티 찾기 (Task의 주인)
        // TodoTask -> Planner -> MenteeProfile -> User 순서로 탐색
        User menteeUser = task.getPlanner().getMentee().getUser();

        // 2. 메시지 결정 (수정인지 신규인지 구분)
        String message = isUpdate
                ? mentor.getName() + " 멘토님이 피드백을 수정했습니다."
                : mentor.getName() + " 멘토님이 피드백을 등록했습니다.";

        // 3. 이벤트 발행
        eventPublisher.publishEvent(new NotificationEvent(
                menteeUser,
                message,
                "/tasks/" + task.getId(), // 클릭 시 이동할 경로
                NotificationType.NEW_FEEDBACK
        ));
    }

}
