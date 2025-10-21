package likelion.harullala.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SampleDataLoader implements CommandLineRunner {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) throws Exception {
        // 샘플 테이블 생성 및 데이터 삽입
        createSampleTables();
        insertSampleData();
    }
    
    private void createSampleTables() {
        // users 테이블 생성 (실제 ERD 구조에 맞게)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                user_id BIGINT PRIMARY KEY,
                user_name VARCHAR(50) NOT NULL,
                email VARCHAR(100) NOT NULL,
                password VARCHAR(255) NOT NULL,
                provider VARCHAR(20) NOT NULL,
                provider_user_id VARCHAR(255) NOT NULL,
                create_date DATE NOT NULL,
                character VARCHAR(20) NOT NULL
            )
        """);
        
        // emotion_record 테이블 생성 (실제 ERD 구조에 맞게)
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS emotion_record (
                record_id BIGINT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                record TEXT NOT NULL,
                emoji_emotion VARCHAR(10) NOT NULL,
                created_at DATE NOT NULL,
                updated_at DATE NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
        """);
    }
    
    private void insertSampleData() {
               // 샘플 사용자 데이터 (현재 User 엔티티 구조에 맞게) - 7명
               jdbcTemplate.update("""
                   MERGE INTO users (user_id, name, nickname, email, provider, provider_user_id, connect_code, created_at, updated_at) VALUES 
                   (1, '테스트유저', '테스트닉네임', 'test@example.com', 'KAKAO', 'kakao_123', 'TEST01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                   (2, '샘플유저', '샘플닉네임', 'sample@example.com', 'APPLE', 'apple_456', 'SAMPLE1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                   (3, '김철수', '철수', 'chulsoo@example.com', 'KAKAO', 'kakao_789', 'CHUL01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                   (4, '이영희', '영희', 'younghee@example.com', 'APPLE', 'apple_012', 'YOUNG1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                   (5, '박민수', '민수', 'minsu@example.com', 'KAKAO', 'kakao_345', 'MINS01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                   (6, '정수진', '수진', 'sujin@example.com', 'APPLE', 'apple_678', 'SUJI01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                   (7, '최지훈', '지훈', 'jihoon@example.com', 'KAKAO', 'kakao_901', 'JIHO01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
               """);
        
        // 샘플 감정 기록 데이터 (현재 EmotionRecord 엔티티 구조에 맞게)
        jdbcTemplate.update("""
            MERGE INTO emotion_record (record_id, user_id, record, emoji_emotion, is_shared, is_deleted, created_at, updated_at) VALUES 
            (1, 1, '오늘은 정말 좋은 하루였어요! 새로운 프로젝트를 시작했는데 기대가 됩니다.', 'HAPPY', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (2, 1, '조금 피곤하지만 뿌듯한 하루였습니다. 팀원들과 좋은 협업을 했어요.', 'CALM', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
            (3, 2, '오늘은 조금 우울한 기분이에요. 하지만 내일은 더 좋을 거예요!', 'SAD', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """);
        
               System.out.println("✅ 샘플 데이터가 로드되었습니다!");
               System.out.println("📊 사용자: 7명, 감정 기록: 3개");
               System.out.println("🔗 현재 엔티티 구조에 맞게 생성되었습니다.");
               System.out.println("👥 사용자 목록:");
               System.out.println("   1. 테스트유저 (TEST01)");
               System.out.println("   2. 샘플유저 (SAMPLE1)");
               System.out.println("   3. 김철수 (CHUL01)");
               System.out.println("   4. 이영희 (YOUNG1)");
               System.out.println("   5. 박민수 (MINS01)");
               System.out.println("   6. 정수진 (SUJI01)");
               System.out.println("   7. 최지훈 (JIHO01)");
    }
}