package likelion.harullala.controller;

import jakarta.validation.Valid;
import likelion.harullala.dto.ApiResponse;
import likelion.harullala.dto.EmotionCreateRequest;
import likelion.harullala.dto.EmotionListResponse;
import likelion.harullala.dto.EmotionResponse;
import likelion.harullala.service.EmotionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emotion")
@RequiredArgsConstructor
public class EmotionRecordController {

    private final EmotionRecordService emotionRecordService;

    /**
     * 감정기록 생성 API
     * POST /api/v1/emotion
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmotionResponse>> createEmotionRecord(
            @Valid @RequestBody EmotionCreateRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        // String token = authorizationHeader.replace("Bearer ", "");
        // Long userId = jwtTokenProvider.getUserIdFromToken(token);
        Long userId = 1L; // 임시 userId

        EmotionResponse response = emotionRecordService.createEmotionRecord(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        201,
                        "감정기록이 성공적으로 저장되었습니다.",
                        response
                ));
    }

    /**
     * 감정기록 목록 조회 API (페이지네이션)
     * GET /api/v1/emotion?page=1&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmotionListResponse>>> getEmotionRecordList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        Long userId = 1L; // 임시 userId

        List<EmotionListResponse> response = emotionRecordService.getEmotionRecordList(userId, page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "감정기록 목록 조회 성공",
                        response
                ));
    }

    /**
     * 감정기록 단일 조회 API
     * GET /api/v1/emotion/{recordId}
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<EmotionResponse>> getEmotionRecord(
            @PathVariable Long recordId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        Long userId = 1L; // 임시 userId

        EmotionResponse response = emotionRecordService.getEmotionRecord(userId, recordId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "감정기록 조회 성공",
                        response
                ));
    }
}


