package com.blaybus.backend.domain.planner.controller;

import com.blaybus.backend.domain.planner.dto.response.WorksheetUploadResponseDto;
import com.blaybus.backend.domain.planner.service.WorksheetService;
import com.blaybus.backend.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mentor/worksheets")
public class WorksheetController {

    private final WorksheetService worksheetService;

    /**
     * Worksheet 파일 업로드 API
     * - multipart/form-data로 file + title + subject (+ materialType 선택) 받음
     * - 로컬(P2)에 파일 저장 후 Worksheet 생성
     * - worksheetId 및 fileUrl 반환
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<WorksheetUploadResponseDto> uploadWorksheet(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @RequestPart("subject") String subject,
            @RequestPart(value = "materialType", required = false) String materialType
    ) {
        // TODO(해커톤): 로그인/인가 붙기 전까지는 임시 mentorId 사용
        // 이후 Security 연동하면 토큰에서 mentorId 추출로 변경
        WorksheetUploadResponseDto data =
                worksheetService.uploadWorksheet(file, title, subject, materialType);

        return ApiResponse.onSuccess(data);
    }
}
