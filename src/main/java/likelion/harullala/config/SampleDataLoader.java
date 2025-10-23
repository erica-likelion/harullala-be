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
        // createSampleTables();
        // insertSampleData();
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
                create_date DATE NOT NULL
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
        // 샘플 사용자 데이터 (실제 ERD 구조에 맞게)
        jdbcTemplate.update("""
            MERGE INTO users (user_id, user_name, email, password, provider, provider_user_id, create_date) VALUES 
            (1, 'testuser', 'test@example.com', 'password123', 'KAKAO', 'kakao_123', CURRENT_DATE),
            (2, 'sampleuser', 'sample@example.com', 'password456', 'APPLE', 'apple_456', CURRENT_DATE)
        """);
        
        // 샘플 감정 기록 데이터 (실제 ERD 구조에 맞게)
        jdbcTemplate.update("""
            MERGE INTO emotion_record (record_id, user_id, record, emoji_emotion, created_at, updated_at) VALUES 
            (1, 1, '오늘은 정말 좋은 하루였어요! 새로운 프로젝트를 시작했는데 기대가 됩니다.', '😊', CURRENT_DATE, CURRENT_DATE),
            (2, 1, '조금 피곤하지만 뿌듯한 하루였습니다. 팀원들과 좋은 협업을 했어요.', '😌', CURRENT_DATE, CURRENT_DATE),
            (3, 2, '오늘은 조금 우울한 기분이에요. 하지만 내일은 더 좋을 거예요!', '😔', CURRENT_DATE, CURRENT_DATE)
        """);
        
        System.out.println("✅ 샘플 데이터가 로드되었습니다!");
        System.out.println("📊 사용자: 2명, 감정 기록: 3개");
        System.out.println("🔗 실제 테이블 구조에 맞게 생성되었습니다.");
    }
}