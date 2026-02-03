package com.blaybus.backend.domain.content.controller;

import com.blaybus.backend.domain.content.dto.StudyColumnSummaryResponse;
import com.blaybus.backend.domain.content.service.StudyColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/columns")
@RequiredArgsConstructor
public class StudyColumnController {

    private final StudyColumnService studyColumnService;

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentColumns(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<StudyColumnSummaryResponse> columns = studyColumnService.getRecentColumns(limit);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Success");
        response.put("data", columns);

        return ResponseEntity.ok(response);
    }
}
