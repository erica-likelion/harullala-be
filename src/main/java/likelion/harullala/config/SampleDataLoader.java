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
        // 테이블은 JPA가 자동으로 생성하므로 여기서는 데이터만 삽입
        System.out.println("📊 JPA가 테이블을 자동 생성합니다...");
    }
    
    private void insertSampleData() {
        try {
            // 샘플 사용자 데이터 삽입 (실제 User 엔티티 구조에 맞게)
            jdbcTemplate.update("""
                INSERT INTO users (user_id, name, nickname, email, provider, provider_user_id, connect_code, created_at, updated_at) VALUES 
                (1, '테스트유저', 'testuser', 'test@example.com', 'KAKAO', 'kakao_123', 'TEST01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (2, '샘플유저', 'sampleuser', 'sample@example.com', 'APPLE', 'apple_456', 'SAMPLE1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (3, '김철수', 'kimcs', 'kim@example.com', 'KAKAO', 'kakao_789', 'CHUL01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (4, '이영희', 'leeyh', 'lee@example.com', 'APPLE', 'apple_012', 'YOUNG1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """);
            
            System.out.println("✅ 샘플 데이터가 로드되었습니다!");
            System.out.println("📊 사용자: 4명");
        } catch (Exception e) {
            System.out.println("⚠️ 샘플 데이터 로드 중 오류 발생: " + e.getMessage());
            // 데이터가 이미 존재하는 경우 무시
        }
    }
}