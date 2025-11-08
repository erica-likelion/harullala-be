package likelion.harullala.controller;

import likelion.harullala.dto.ApiResponse;
import likelion.harullala.dto.EmotionReportComparisonResponse;
import likelion.harullala.dto.EmotionReportTopEmotionsResponse;
import likelion.harullala.dto.EmotionReportTimePatternResponse;
import likelion.harullala.service.EmotionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 감정 리포트 컨트롤러
 * 통계 및 분석 API 제공
 */
@RestController
@RequestMapping("/api/v1/emotion/report")
@RequiredArgsConstructor
public class EmotionReportController {

    private final EmotionReportService emotionReportService;

    /**
     * 저번 달과 이번 달 감정 상태 비교 API
     * GET /api/v1/emotion/report/comparison?month=2024-01
     * 
     * 화면: 저번 달과 상태 비교
     * - X축 평균, Y축 평균 좌표로 두 개의 원 표시
     * - 저번 달: 왼쪽 하단 (예시)
     * - 이번 달: 오른쪽 상단 (예시)
     * 
     * @param month 대상 월 (yyyy-MM 형식, 생략 시 현재 월)
     * @param authorizationHeader JWT 토큰
     * @return 월별 평균 비교 데이터
     */
    @GetMapping("/comparison")
    public ResponseEntity<ApiResponse<EmotionReportComparisonResponse>> getMonthlyComparison(
            @RequestParam(required = false) String month,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        Long userId = 1L;

        EmotionReportComparisonResponse response = 
                emotionReportService.compareMonthlyEmotions(userId, month);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "월별 감정 상태 비교 조회 성공",
                        response
                ));
    }

    /**
     * 가장 많았던 감정 통계 API
     * GET /api/v1/emotion/report/top-emotions?month=2024-01
     * 
     * 화면: 제일 많았던 상태
     * - 감정별 횟수 내림차순 정렬
     * - 감정명 + 횟수 + 색상 바 표시
     * 
     * @param month 대상 월 (yyyy-MM 형식, 생략 시 현재 월)
     * @param authorizationHeader JWT 토큰
     * @return 감정별 통계 (내림차순)
     */
    @GetMapping("/top-emotions")
    public ResponseEntity<ApiResponse<EmotionReportTopEmotionsResponse>> getTopEmotions(
            @RequestParam(required = false) String month,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출 (현재는 임시로 1L 사용)
        Long userId = 1L;

        EmotionReportTopEmotionsResponse response = 
                emotionReportService.getTopEmotions(userId, month);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "가장 많았던 감정 통계 조회 성공",
                        response
                ));
    }

    /**
     * 리포트 메인 요약 API (선택사항)
     * GET /api/v1/emotion/report/summary?month=2024-01
     * 
     * 메인 화면에 표시할 요약 정보
     * - 저번 달 대비 변화
     * - 가장 많았던 감정 Top 3
     * - 전체 기록 수
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReportSummary>> getReportSummary(
            @RequestParam(required = false) String month,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출
        Long userId = 1L;

        // 비교 데이터 가져오기
        EmotionReportComparisonResponse comparisonData = 
                emotionReportService.compareMonthlyEmotions(userId, month);

        // 통계 데이터 가져오기
        EmotionReportTopEmotionsResponse topEmotionsData = 
                emotionReportService.getTopEmotions(userId, month);

        // 요약 데이터 구성
        ReportSummary summary = new ReportSummary(comparisonData, topEmotionsData);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "리포트 요약 조회 성공",
                        summary
                ));
    }

    /**
     * 시간대별 감정 패턴 분석 API
     * GET /api/v1/emotion/report/time-pattern?month=2024-01
     * 
     * 화면: 특정 상태가 많았던 시간대
     * - 4개 시간대(새벽, 아침, 낮, 저녁)별 통계
     * - 가장 많이 기록한 시간대와 주요 감정 표시
     * 
     * @param month 대상 월 (yyyy-MM 형식, 생략 시 현재 월)
     * @param authorizationHeader JWT 토큰
     * @return 시간대별 감정 패턴
     */
    @GetMapping("/time-pattern")
    public ResponseEntity<ApiResponse<EmotionReportTimePatternResponse>> getTimePattern(
            @RequestParam(required = false) String month,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        // TODO: JWT 토큰에서 userId 추출
        Long userId = 1L;

        EmotionReportTimePatternResponse response = 
                emotionReportService.getTimePattern(userId, month);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        200,
                        "시간대별 감정 패턴 조회 성공",
                        response
                ));
    }

    /**
     * 리포트 요약 DTO (내부 클래스)
     */
    public record ReportSummary(
            EmotionReportComparisonResponse comparison,
            EmotionReportTopEmotionsResponse topEmotions
    ) {}
}

