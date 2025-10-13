package likelion.harullala.controller;

import jakarta.validation.Valid;
import likelion.harullala.dto.ApiResponse;
import likelion.harullala.dto.EmotionCreateRequest;
import likelion.harullala.dto.EmotionResponse;
import likelion.harullala.service.EmotionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}


