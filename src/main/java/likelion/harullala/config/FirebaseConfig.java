package likelion.harullala.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URL;

/**
 * Firebase 초기화 설정
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_CREDENTIALS_PATH}")
    private String firebaseCredentialsPath;  // 예: file:/app/firebase-service-account.json

    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("FirebaseApp already initialized. Skipping.");
                return;
            }

            // file: 프리픽스를 포함한 URL로부터 스트림을 연다
            URL url = new URL(firebaseCredentialsPath);
            try (InputStream serviceAccount = url.openStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase 초기화 완료: path={}", firebaseCredentialsPath);
            }

        } catch (Exception e) {
            log.error("Firebase 초기화 실패: {}", e.getMessage(), e);
            log.warn("푸시 알림 기능이 비활성화됩니다. Firebase 설정(FIREBASE_CREDENTIALS_PATH)을 확인해주세요.");
        }
    }
}

