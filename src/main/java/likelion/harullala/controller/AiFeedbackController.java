package likelion.harullala.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import likelion.harullala.dto.ApiResponse;
import likelion.harullala.dto.CreateFeedbackRequest;
import likelion.harullala.dto.FeedbackDto;
import likelion.harullala.service.AiFeedbackService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
class AiFeedbackController {
    private final AiFeedbackService service;

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackDto>> create(@RequestBody CreateFeedbackRequest req) {
        var dto = service.createOrRegenerate(req);
        return ResponseEntity.ok(ApiResponse.ok("AI 피드백 생성 성공", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FeedbackDto>> get(@RequestParam Long recordId) {
        var dto = service.fetchByRecordId(recordId);
        return ResponseEntity.ok(ApiResponse.ok("AI 피드백 조회 성공", dto));
    }
}