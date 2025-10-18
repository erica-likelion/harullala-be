package likelion.harullala.controller;

import jakarta.validation.Valid;
import likelion.harullala.dto.ApiResponse;
import likelion.harullala.dto.EmotionCreateRequest;
import likelion.harullala.dto.EmotionDeleteResponse;
import likelion.harullala.dto.EmotionListResponse;
import likelion.harullala.dto.EmotionResponse;
import likelion.harullala.dto.EmotionShareRequest;
import likelion.harullala.dto.EmotionUpdateRequest;
import likelion.harullala.dto.EmotionUpdateResponse;
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
    private final likelion.harullala.service.EmotionRecommendService emotionRecommendService;

    /**
     * 감정 추천 API (색상/좌표 기반)
     * POST /api/v1/emotion/recommend
     * 
     * 플로우: 사용자가 그라디언트 맵에서 색상 선택 → 좌표/색상 정보 전송 → 4-5개 감정 추천
     */
    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<likelion.harullala.dto.EmotionRecommendResponse>> recommendEmotions(
            @Valid @RequestBody likelion.harullala.dto.EmotionRecommendRequest request
    ) {
        likelion.harullala.dto.EmotionRecommendResponse response = emotionRecommendService.recommendEmotions(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "감정 추천 완료",
                        response
                ));
    }

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
     * 공유된 감정기록 목록 조회 API (페이지네이션)
     * GET /api/v1/emotion/shared?page=1&size=20
     */
    @GetMapping("/shared")
    public ResponseEntity<ApiResponse<List<EmotionListResponse>>> getSharedEmotionRecordList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<EmotionListResponse> response = emotionRecordService.getSharedEmotionRecordList(page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "공유된 감정기록 목록 조회 성공",
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

    /**
     * 감정기록 수정 API
     * PUT /api/v1/emotion/{recordId}
     */
    @PutMapping("/{recordId}")
    public ResponseEntity<ApiResponse<EmotionUpdateResponse>> updateEmotionRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody EmotionUpdateRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        Long userId = 1L; // 임시 userId

        EmotionUpdateResponse response = emotionRecordService.updateEmotionRecord(userId, recordId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "감정기록 수정 완료",
                        response
                ));
    }

    /**
     * 감정기록 삭제 API
     * DELETE /api/v1/emotion/{recordId}
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<EmotionDeleteResponse>> deleteEmotionRecord(
            @PathVariable Long recordId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        Long userId = 1L; // 임시 userId

        EmotionDeleteResponse response = emotionRecordService.deleteEmotionRecord(userId, recordId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "감정기록이 삭제되었습니다.",
                        response
                ));
    }

    /**
     * 감정기록 공유 상태 변경 API
     * PATCH /api/v1/emotion/{recordId}/share
     */
    @PatchMapping("/{recordId}/share")
    public ResponseEntity<ApiResponse<EmotionResponse>> updateSharedStatus(
            @PathVariable Long recordId,
            @Valid @RequestBody EmotionShareRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        Long userId = 1L; // 임시 userId

        EmotionResponse response = emotionRecordService.updateSharedStatus(userId, recordId, request.getIs_shared());

        String message = request.getIs_shared() ? "감정기록이 공유되었습니다." : "감정기록 공유가 취소되었습니다.";

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        message,
                        response
                ));
    }
}


