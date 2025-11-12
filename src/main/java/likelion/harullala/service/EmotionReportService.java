package likelion.harullala.service;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.dto.EmotionReportCharacterMessageResponse;
import likelion.harullala.dto.EmotionReportComparisonResponse;
import likelion.harullala.dto.EmotionReportTopEmotionsResponse;
import likelion.harullala.dto.EmotionReportTimePatternResponse;
import likelion.harullala.infra.ChatGptClient;
import likelion.harullala.repository.EmotionRecordRepository;
import likelion.harullala.repository.UserCharacterRepository;
import likelion.harullala.util.EmotionCoordinateMapper;
import likelion.harullala.util.EmotionCoordinateMapper.Coordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.harullala.exception.ApiException;

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
    private final UserCharacterRepository userCharacterRepository;
    private final ChatGptClient chatGptClient;

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
                    
                    // 첫 번째 레코드의 색상 사용
                    EmotionRecord firstRecord = emotionRecords.get(0);
                    String mainColor = firstRecord.getMainColor();
                    String subColor = firstRecord.getSubColor();
                    String textColor = firstRecord.getTextColor();
                    
                    // 비율 계산
                    double percentage = (count * 100.0) / totalCount;
                    
                    return EmotionReportTopEmotionsResponse.EmotionStat.builder()
                            .emotion_name(emotionName)
                            .count(count)
                            .main_color(mainColor)
                            .sub_color(subColor)
                            .text_color(textColor)
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
                    .representative_emotion("없음") // 대표 감정 없음
                    .main_color("#808080") // 회색
                    .sub_color("#808080") // 회색
                    .text_color("#000000") // 검정
                    .label(label)
                    .record_count(0)
                    .build();
        }

        // X, Y 좌표 평균 계산 (감정명 기반)
        // 각 감정기록의 감정명으로 미리 정의된 좌표를 가져와서 평균 계산
        double avgX = records.stream()
                .map(r -> EmotionCoordinateMapper.getCoordinate(r.getEmotionName()))
                .mapToDouble(Coordinate::getX)
                .average()
                .orElse(0.5);

        double avgY = records.stream()
                .map(r -> EmotionCoordinateMapper.getCoordinate(r.getEmotionName()))
                .mapToDouble(Coordinate::getY)
                .average()
                .orElse(0.5);

        // 가장 많이 사용된 감정 찾기 (대표 감정)
        Map.Entry<String, List<EmotionRecord>> mostFrequentEmotion = findMostFrequentEmotion(records);
        String representativeEmotion = "없음";
        String mainColor = "#808080";
        String subColor = "#808080";
        String textColor = "#000000";

        if (mostFrequentEmotion != null && !mostFrequentEmotion.getValue().isEmpty()) {
            representativeEmotion = mostFrequentEmotion.getKey();
            EmotionRecord representativeRecord = mostFrequentEmotion.getValue().get(0);
            mainColor = representativeRecord.getMainColor();
            subColor = representativeRecord.getSubColor();
            textColor = representativeRecord.getTextColor();
        }

        return EmotionReportComparisonResponse.MonthlyAverage.builder()
                .month(month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .avg_position_x(Math.round(avgX * 100.0) / 100.0) // 소수점 2자리
                .avg_position_y(Math.round(avgY * 100.0) / 100.0)
                .representative_emotion(representativeEmotion)
                .main_color(mainColor)
                .sub_color(subColor)
                .text_color(textColor)
                .label(label)
                .record_count(records.size())
                .build();
    }

    /**
     * 가장 많이 사용된 감정 찾기 (대표 감정)
     */
    private Map.Entry<String, List<EmotionRecord>> findMostFrequentEmotion(List<EmotionRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(EmotionRecord::getEmotionName))
                .entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .orElse(null);
    }

    /**
     * 시간대별 감정 패턴 분석
     * @param userId 사용자 ID
     * @param targetMonth 대상 월 (예: "2024-01", null이면 현재 월)
     * @return 시간대별 감정 패턴
     */
    public EmotionReportTimePatternResponse getTimePattern(Long userId, String targetMonth) {
        // 대상 월 설정
        YearMonth month = targetMonth != null 
                ? YearMonth.parse(targetMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
                : YearMonth.now();

        // 해당 월의 감정 기록 조회
        List<EmotionRecord> records = getMonthlyRecords(userId, month);

        if (records.isEmpty()) {
            return EmotionReportTimePatternResponse.builder()
                    .dominant_time("없음")
                    .dominant_emotion("없음")
                    .total_count(0)
                    .time_slots(Collections.emptyList())
                    .build();
        }

        int totalCount = records.size();

        // 시간대별로 그룹화
        Map<String, List<EmotionRecord>> timeGroups = records.stream()
                .collect(Collectors.groupingBy(this::getTimeSlotName));

        // 각 시간대별 통계 생성
        List<EmotionReportTimePatternResponse.TimeSlot> timeSlots = timeGroups.entrySet().stream()
                .map(entry -> {
                    String timeSlot = entry.getKey();
                    List<EmotionRecord> timeRecords = entry.getValue();
                    int count = timeRecords.size();

                    // 해당 시간대에서 가장 많았던 감정 찾기
                    Map<String, List<EmotionRecord>> emotionGroups = timeRecords.stream()
                            .collect(Collectors.groupingBy(EmotionRecord::getEmotionName));

                    // 가장 많이 나타난 감정
                    Map.Entry<String, List<EmotionRecord>> topEmotion = emotionGroups.entrySet().stream()
                            .max(Comparator.comparingInt(e -> e.getValue().size()))
                            .orElse(null);

                    if (topEmotion == null) {
                        return null;
                    }

                    String emotionName = topEmotion.getKey();
                    List<EmotionRecord> emotionRecords = topEmotion.getValue();
                    EmotionRecord firstRecord = emotionRecords.get(0);
                    String mainColor = firstRecord.getMainColor();
                    String subColor = firstRecord.getSubColor();
                    String textColor = firstRecord.getTextColor();
                    double percentage = (count * 100.0) / totalCount;

                    return EmotionReportTimePatternResponse.TimeSlot.builder()
                            .time_range(getTimeRangeLabel(timeSlot))
                            .emotion_name(emotionName)
                            .count(count)
                            .percentage(Math.round(percentage * 10) / 10.0)
                            .main_color(mainColor)
                            .sub_color(subColor)
                            .text_color(textColor)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getCount().compareTo(a.getCount())) // 기록 수 내림차순
                .collect(Collectors.toList());

        // 가장 많이 기록한 시간대와 그 시간대의 주요 감정
        EmotionReportTimePatternResponse.TimeSlot dominantSlot = timeSlots.isEmpty() ? null : timeSlots.get(0);

        return EmotionReportTimePatternResponse.builder()
                .dominant_time(dominantSlot != null ? extractTimeName(dominantSlot.getTime_range()) : "없음")
                .dominant_emotion(dominantSlot != null ? dominantSlot.getEmotion_name() : "없음")
                .total_count(totalCount)
                .time_slots(timeSlots)
                .build();
    }

    /**
     * 시간대 구분 (0-6: 새벽, 6-12: 아침, 12-18: 낮, 18-24: 저녁)
     */
    private String getTimeSlotName(EmotionRecord record) {
        int hour = record.getCreatedAt().getHour();
        
        if (hour >= 0 && hour < 6) {
            return "새벽";
        } else if (hour >= 6 && hour < 12) {
            return "아침";
        } else if (hour >= 12 && hour < 18) {
            return "낮";
        } else {
            return "저녁";
        }
    }

    /**
     * 시간대 라벨 생성
     */
    private String getTimeRangeLabel(String timeSlot) {
        switch (timeSlot) {
            case "새벽":
                return "새벽 (00:00-06:00)";
            case "아침":
                return "아침 (06:00-12:00)";
            case "낮":
                return "낮 (12:00-18:00)";
            case "저녁":
                return "저녁 (18:00-24:00)";
            default:
                return timeSlot;
        }
    }

    /**
     * 시간대 라벨에서 시간대 이름만 추출
     */
    private String extractTimeName(String timeRangeLabel) {
        // "낮 (12:00-18:00)" -> "낮"
        int index = timeRangeLabel.indexOf(" ");
        return index > 0 ? timeRangeLabel.substring(0, index) : timeRangeLabel;
    }

    /**
     * 캐릭터 멘트 생성
     * @param userId 사용자 ID
     * @param targetMonth 대상 월 (예: "2024-01", null이면 현재 월)
     * @return 캐릭터 멘트 응답
     */
    @Transactional
    public EmotionReportCharacterMessageResponse generateCharacterMessage(Long userId, String targetMonth) {
        // 대상 월 설정
        YearMonth month = targetMonth != null 
                ? YearMonth.parse(targetMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
                : YearMonth.now();

        // 사용자의 활성 캐릭터 조회
        UserCharacter userCharacter = userCharacterRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "활성 캐릭터를 찾을 수 없습니다."));
        
        Character character = userCharacter.getSelectedCharacter();

        // 리포트 데이터 조회
        EmotionReportTopEmotionsResponse topEmotions = getTopEmotions(userId, targetMonth);
        EmotionReportTimePatternResponse timePattern = getTimePattern(userId, targetMonth);

        // 리포트 요약 텍스트 생성
        String reportSummary = buildReportSummary(topEmotions, timePattern, month);

        // AI로 캐릭터 멘트 생성
        String message = chatGptClient.generateReportMessage(reportSummary, character);

        return EmotionReportCharacterMessageResponse.builder()
                .character_name(character.getName())
                .character_image_url(character.getImageUrl())
                .message(message)
                .build();
    }

    /**
     * 리포트 요약 텍스트 생성
     */
    private String buildReportSummary(
            EmotionReportTopEmotionsResponse topEmotions,
            EmotionReportTimePatternResponse timePattern,
            YearMonth month) {
        
        StringBuilder summary = new StringBuilder();
        summary.append(month.format(DateTimeFormatter.ofPattern("yyyy년 M월"))).append(" 감정 리포트\n\n");
        
        // 전체 기록 수
        summary.append("총 ").append(topEmotions.getTotal_count()).append("번의 감정 기록\n\n");
        
        // 가장 많았던 감정 (Top 3)
        summary.append("가장 많았던 감정:\n");
        List<EmotionReportTopEmotionsResponse.EmotionStat> emotions = topEmotions.getEmotions();
        int topCount = Math.min(3, emotions.size());
        for (int i = 0; i < topCount; i++) {
            EmotionReportTopEmotionsResponse.EmotionStat emotion = emotions.get(i);
            summary.append(i + 1).append(". ")
                   .append(emotion.getEmotion_name())
                   .append(" - ")
                   .append(emotion.getCount()).append("번 (")
                   .append(emotion.getPercentage()).append("%)\n");
        }
        
        // 시간대 패턴
        summary.append("\n시간대별 패턴:\n");
        summary.append("가장 많이 기록한 시간대: ").append(timePattern.getDominant_time()).append("\n");
        summary.append("그 시간대의 주요 감정: ").append(timePattern.getDominant_emotion()).append("\n");
        
        return summary.toString();
    }
}

