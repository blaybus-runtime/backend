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
@RequestMapping("/api/v1/mentee/worksheets")
public class MenteeWorksheetController {

    private final WorksheetService worksheetService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<WorksheetUploadResponseDto> uploadWorksheet(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") String title,
            @RequestPart("subject") String subject,
            @RequestPart(value = "materialType", required = false) String materialType
    ) {
        WorksheetUploadResponseDto data =
                worksheetService.uploadWorksheetAsMentee(file, title, subject, materialType);

        return ApiResponse.onSuccess(data);
    }
}
