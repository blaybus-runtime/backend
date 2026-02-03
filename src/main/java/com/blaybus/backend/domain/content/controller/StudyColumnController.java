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

    /*
    /api/v1/columns/recent?limit=5 를 처리하는 api 입니다.
    limit로 전달한 개수만큼 최근 칼럼을 조회해서 반환합니다. 파라미터 미지정시 5개만 가져와서 반환합니다.
    홈 화면에 칼럼을 넣는 용도로 설계한 api라서 정보를 요약해서 전달했습니다. 전달 형식은 api 명세를 참고해주세요.
    - 김동수 -
    */
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
