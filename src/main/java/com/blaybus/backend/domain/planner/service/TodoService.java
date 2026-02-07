package com.blaybus.backend.domain.planner.service;

import com.blaybus.backend.domain.content.Worksheet;
import com.blaybus.backend.domain.content.service.R2StorageService;
import com.blaybus.backend.domain.planner.StudyPlanner;
import com.blaybus.backend.domain.planner.TaskWorksheet;
import com.blaybus.backend.domain.planner.TodoTask;
import com.blaybus.backend.domain.planner.dto.response.TaskDetailResponse; // 추가된 import
import com.blaybus.backend.domain.planner.dto.response.TodoTaskSortedResponse;
import com.blaybus.backend.domain.planner.repository.StudyPlannerRepository;
import com.blaybus.backend.domain.planner.repository.TodoRepository;
import com.blaybus.backend.global.exception.GeneralException; // 추가된 import
import com.blaybus.backend.global.response.status.ErrorStatus; // 추가된 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final StudyPlannerRepository studyPlannerRepository;
    private final R2StorageService r2StorageService;

    // [기존] 멘토용 할 일 정렬 조회
    public List<TodoTaskSortedResponse> getSortedTasksByMenteeAndDate(Long menteeId, LocalDate date) {

        StudyPlanner planner = studyPlannerRepository.findByMenteeIdAndPlanDate(menteeId, date)
                .orElse(null);

        if (planner == null) {
            return List.of();
        }

        List<TodoTask> tasks = todoRepository.findAllByPlannerIdWithFeedback(planner.getId());

        return tasks.stream()
                .sorted(Comparator.comparingInt(this::calculatePriority))
                .map(TodoTaskSortedResponse::from)
                .toList();
    }

    /**
     * [추가] 할 일 상세 조회 (학습지, 제출물, 피드백 포함)
     * - 엔티티 구조 변경(TaskWorksheet)에 대한 처리는 TaskDetailResponse.from() 내부에서 수행됩니다.
     */
    public TaskDetailResponse getTaskDetail(Long taskId) {
        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._TASK_NOT_FOUND));

        return TaskDetailResponse.from(task);
    }

    // [기존] 우선순위 계산 헬퍼
    private int calculatePriority(TodoTask task) {
        boolean isTaskDone = task.isCompleted();
        boolean isFeedbackDone = (task.getFeedback() != null);

        if (isTaskDone && !isFeedbackDone) {
            return 1; // 1순위
        } else if (!isTaskDone && !isFeedbackDone) {
            return 2; // 2순위
        } else {
            return 3; // 3순위 (피드백 완료)
        }
    }

    /**
     * [추가] 학습지 ZIP 다운로드 처리
     */
    public void downloadWorksheetsAsZip(Long taskId, OutputStream responseOutputStream) {
        // 1. Task 조회
        TodoTask task = todoRepository.findById(taskId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._TASK_NOT_FOUND));

        // 2. 연결된 Worksheet 목록 추출 (N:M 구조 대응)
        List<Worksheet> worksheets = task.getTaskWorksheets().stream()
                .map(TaskWorksheet::getWorksheet)
                .toList();

        if (worksheets.isEmpty()) {
            throw new GeneralException(ErrorStatus._WORKSHEET_NOT_FOUND); // 혹은 빈 ZIP을 보내거나 처리
        }

        // 3. ZipOutputStream 생성 및 파일 쓰기
        try (ZipOutputStream zos = new ZipOutputStream(responseOutputStream)) {

            for (Worksheet worksheet : worksheets) {
                // (A) 파일 URL이 없으면 스킵
                if (!StringUtils.hasText(worksheet.getFileUrl())) continue;

                try {
                    // (B) URL에서 S3/R2 Object Key 추출 (팀원 코드 활용)
                    String objectKey = r2StorageService.extractKeyFromUrl(worksheet.getFileUrl());

                    // (C) 파일명 결정 (중복 방지를 위해 ID 붙임 & 확장자 유지)
                    String fileName = createUniqueFileName(worksheet, objectKey);

                    // (D) Zip Entry 추가
                    zos.putNextEntry(new ZipEntry(fileName));

                    // (E) R2에서 스트림으로 읽어서 -> Zip 스트림으로 복사 (메모리 효율적)
                    try (InputStream r2InputStream = r2StorageService.download(objectKey)) {
                        r2InputStream.transferTo(zos);
                    }

                    zos.closeEntry();

                } catch (Exception e) {
                    // 특정 파일 다운로드 실패해도 로그 찍고 다음 파일 진행 (선택사항)
                    log.error("파일 다운로드 실패: {}", worksheet.getTitle(), e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("ZIP 압축 중 오류 발생", e);
        }
    }

    // 파일명 중복 방지 및 확장자 처리 헬퍼
    private String createUniqueFileName(Worksheet worksheet, String objectKey) {
        // 원래 파일의 확장자 추출 (예: .pdf)
        String extension = "";
        int dotIndex = objectKey.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = objectKey.substring(dotIndex);
        }

        // 파일명: "제목_ID.확장자" (예: 국어_보충자료_101.pdf)
        // 제목에 공백이나 특수문자가 있을 수 있으니 안전하게 처리해도 됨
        return worksheet.getTitle() + "_" + worksheet.getId() + extension;
    }
}