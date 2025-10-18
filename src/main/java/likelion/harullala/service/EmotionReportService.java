package likelion.harullala.service;

import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.dto.EmotionReportComparisonResponse;
import likelion.harullala.dto.EmotionReportTopEmotionsResponse;
import likelion.harullala.repository.EmotionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 감정 리포트 서비스
 * 통계 및 분석 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionReportService {

    private final EmotionRecordRepository emotionRecordRepository;

    /**
     * 저번 달과 이번 달 감정 상태 비교
     * @param userId 사용자 ID
     * @param targetMonth 대상 월 (예: "2024-01", null이면 현재 월)
     * @return 월별 평균 비교 데이터
     */
    public EmotionReportComparisonResponse compareMonthlyEmotions(Long userId, String targetMonth) {
        // 대상 월 설정 (null이면 현재 월)
        YearMonth thisMonth = targetMonth != null 
                ? YearMonth.parse(targetMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
                : YearMonth.now();
        
        YearMonth lastMonth = thisMonth.minusMonths(1);

        // 각 월의 감정 기록 조회
        List<EmotionRecord> thisMonthRecords = getMonthlyRecords(userId, thisMonth);
        List<EmotionRecord> lastMonthRecords = getMonthlyRecords(userId, lastMonth);

        // 월별 평균 계산
        EmotionReportComparisonResponse.MonthlyAverage thisMonthAvg = 
                calculateMonthlyAverage(thisMonthRecords, thisMonth, "이번 달 평균 상태");
        
        EmotionReportComparisonResponse.MonthlyAverage lastMonthAvg = 
                calculateMonthlyAverage(lastMonthRecords, lastMonth, "저번 달 평균 상태");

        return EmotionReportComparisonResponse.builder()
                .thisMonth(thisMonthAvg)
                .lastMonth(lastMonthAvg)
                .build();
    }

    /**
     * 가장 많았던 감정 통계 (내림차순)
     * @param userId 사용자 ID
     * @param targetMonth 대상 월 (예: "2024-01", null이면 현재 월)
     * @return 감정별 횟수 통계
     */
    public EmotionReportTopEmotionsResponse getTopEmotions(Long userId, String targetMonth) {
        // 대상 월 설정
        YearMonth month = targetMonth != null 
                ? YearMonth.parse(targetMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
                : YearMonth.now();

        // 해당 월의 감정 기록 조회
        List<EmotionRecord> records = getMonthlyRecords(userId, month);

        if (records.isEmpty()) {
            return EmotionReportTopEmotionsResponse.builder()
                    .emotions(Collections.emptyList())
                    .total_count(0)
                    .build();
        }

        // 감정명별로 그룹화하여 횟수 집계
        Map<String, List<EmotionRecord>> emotionGroups = records.stream()
                .collect(Collectors.groupingBy(EmotionRecord::getEmotionName));

        int totalCount = records.size();

        // 통계 생성 및 정렬 (내림차순)
        List<EmotionReportTopEmotionsResponse.EmotionStat> stats = emotionGroups.entrySet().stream()
                .map(entry -> {
                    String emotionName = entry.getKey();
                    List<EmotionRecord> emotionRecords = entry.getValue();
                    int count = emotionRecords.size();
                    
                    // 가장 많이 사용된 색상 찾기 (Main Color 기준)
                    String representativeColor = findMostFrequentColor(emotionRecords);
                    
                    // 첫 번째 레코드의 카테고리 사용
                    var emojiEmotion = emotionRecords.get(0).getEmojiEmotion();
                    
                    // 비율 계산
                    double percentage = (count * 100.0) / totalCount;
                    
                    return EmotionReportTopEmotionsResponse.EmotionStat.builder()
                            .emotion_name(emotionName)
                            .emoji_emotion(emojiEmotion)
                            .count(count)
                            .color(representativeColor)
                            .percentage(Math.round(percentage * 10) / 10.0) // 소수점 1자리
                            .build();
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount())) // 내림차순 정렬
                .collect(Collectors.toList());

        return EmotionReportTopEmotionsResponse.builder()
                .emotions(stats)
                .total_count(totalCount)
                .build();
    }

    /**
     * 특정 월의 감정 기록 조회
     */
    private List<EmotionRecord> getMonthlyRecords(Long userId, YearMonth month) {
        LocalDateTime startDate = month.atDay(1).atStartOfDay();
        LocalDateTime endDate = month.plusMonths(1).atDay(1).atStartOfDay();
        
        return emotionRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    /**
     * 월별 평균 계산
     */
    private EmotionReportComparisonResponse.MonthlyAverage calculateMonthlyAverage(
            List<EmotionRecord> records, YearMonth month, String label) {
        
        if (records.isEmpty()) {
            return EmotionReportComparisonResponse.MonthlyAverage.builder()
                    .month(month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .avg_position_x(0.5) // 중앙값
                    .avg_position_y(0.5) // 중앙값
                    .avg_color("#808080") // 회색
                    .label(label)
                    .record_count(0)
                    .build();
        }

        // X, Y 좌표 평균 계산
        double avgX = records.stream()
                .filter(r -> r.getPositionX() != null)
                .mapToDouble(EmotionRecord::getPositionX)
                .average()
                .orElse(0.5);

        double avgY = records.stream()
                .filter(r -> r.getPositionY() != null)
                .mapToDouble(EmotionRecord::getPositionY)
                .average()
                .orElse(0.5);

        // 가장 많이 사용된 색상 찾기
        String avgColor = findMostFrequentColor(records);

        return EmotionReportComparisonResponse.MonthlyAverage.builder()
                .month(month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .avg_position_x(Math.round(avgX * 100.0) / 100.0) // 소수점 2자리
                .avg_position_y(Math.round(avgY * 100.0) / 100.0)
                .avg_color(avgColor)
                .label(label)
                .record_count(records.size())
                .build();
    }

    /**
     * 가장 많이 사용된 색상 찾기 (Main Color 기준)
     */
    private String findMostFrequentColor(List<EmotionRecord> records) {
        return records.stream()
                .map(EmotionRecord::getMainColor)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(color -> color, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("#808080"); // 기본값: 회색
    }
}

