# 🔔 푸시 알림 기능 구현 (AOIF-51)

## 📋 개요
사용자에게 실시간 푸시 알림 및 알림함 기능을 제공합니다.

---

## ✨ 주요 기능

### 1. 알림 타입 (4가지)
- **AI 피드백 도착**: AI 피드백 생성 시 알림
- **친구 요청 받음**: 친구 요청을 받았을 때 알림
- **친구 요청 수락**: 친구 요청이 수락되었을 때 알림
- **친구 감정 기록 작성**: 친구가 감정 기록을 작성했을 때 알림

### 2. 알림 API (4개)
- `POST /api/v1/notifications/token` - FCM 토큰 등록
- `GET /api/v1/notifications` - 알림 목록 조회 (페이징, 날짜 필터)
- `GET /api/v1/notifications/unread-count` - 안읽은 알림 개수
- `PUT /api/v1/notifications/{id}/read` - 알림 읽음 처리

### 3. Firebase Cloud Messaging (FCM) 연동
- Firebase Admin SDK 설정
- 실시간 푸시 알림 전송
- 기기별 FCM 토큰 관리

---

## 🗄️ 데이터베이스 변경

### notifications 테이블 추가
```sql
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    related_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME,
    created_at DATETIME NOT NULL,
    INDEX idx_notification_user_created (user_id, created_at DESC),
    INDEX idx_notification_user_read (user_id, is_read),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### users 테이블 수정
```sql
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(500);
```

---

## 📁 주요 파일

### 새로 추가된 파일
- `domain/Notification.java` - 알림 엔티티
- `domain/NotificationType.java` - 알림 타입 enum
- `repository/NotificationRepository.java` - 알림 Repository
- `service/NotificationService.java` - 알림 비즈니스 로직
- `controller/NotificationController.java` - 알림 API
- `config/FirebaseConfig.java` - Firebase 설정
- `dto/NotificationResponse.java` - 알림 응답 DTO
- `dto/NotificationListResponse.java` - 알림 목록 응답 DTO
- `dto/UnreadCountResponse.java` - 안읽은 알림 개수 DTO
- `dto/FcmTokenRequest.java` - FCM 토큰 요청 DTO

### 수정된 파일
- `domain/User.java` - fcmToken 필드 추가
- `service/AiFeedbackService.java` - AI 피드백 알림 전송
- `service/impl/FriendServiceImpl.java` - 친구 관련 알림 전송
- `service/EmotionRecordService.java` - 친구 감정 기록 알림 전송
- `build.gradle` - Firebase Admin SDK 의존성 추가
- `application.yml` - Firebase 설정 추가
- `.gitignore` - firebase-service-account.json 제외

---

## 🔄 동작 흐름

### 알림 발송
```
이벤트 발생 (AI 피드백, 친구 요청 등)
    ↓
NotificationService.sendNotification()
    ↓
1. DB에 알림 저장
2. user.getFcmToken() 조회
3. Firebase로 푸시 전송
    ↓
사용자 기기에 알림 표시
```

### 알림 조회
```
앱에서 GET /api/v1/notifications 호출
    ↓
DB에서 알림 목록 조회 (페이징)
    ↓
알림 목록 반환
```

---

## 🚀 배포 시 필요 작업

### 1. 환경 변수 설정
```bash
FIREBASE_CREDENTIALS_PATH=/path/to/firebase-service-account.json
```

### 2. Firebase 설정 파일 배치
- `firebase-service-account.json` 파일을 서버의 안전한 위치에 배치
- 경로를 환경 변수로 설정

### 3. DB 마이그레이션
```sql
-- notifications 테이블은 자동 생성됨 (JPA)
-- users 테이블만 수동 마이그레이션 필요
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(500);
```

---

## 📱 앱 팀원 작업 사항

### 1. Firebase 설정
- Firebase 프로젝트에서 앱 등록 (Android/iOS)
- `google-services.json` (Android) 또는 `GoogleService-Info.plist` (iOS) 추가

### 2. FCM 토큰 등록
```kotlin
// Android 예시
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    val token = task.result
    // POST /api/v1/notifications/token
    api.registerFcmToken(FcmTokenRequest(token))
}
```

### 3. 푸시 알림 수신 처리
```kotlin
// 푸시 데이터 구조
{
    "type": "AI_FEEDBACK",
    "title": "AI 피드백이 도착했어요",
    "message": "오늘의 감정에 대한...",
    "relatedId": "123"
}
```

### 4. 알림 클릭 시 화면 이동
- `AI_FEEDBACK` → AI 피드백 상세 화면
- `FRIEND_REQUEST` → 친구 요청 화면
- `FRIEND_ACCEPTED` → 친구 목록 화면
- `FRIEND_EMOTION_RECORD` → 친구 감정 기록 상세 화면

---

## ✅ 테스트

### 빌드 테스트
```bash
./gradlew clean build -x test
# BUILD SUCCESSFUL
```

### 주요 테스트 항목
- [x] 알림 엔티티 생성
- [x] 알림 API 4개 구현
- [x] Firebase 설정
- [x] FCM 푸시 기능
- [x] 이벤트별 알림 전송
- [x] User.java fcmToken 추가
- [x] 빌드 성공

---

## 🔐 보안

### .gitignore 설정
```gitignore
firebase-service-account.json
```

### 주의사항
- Firebase 서비스 계정 키 파일은 절대 Git에 커밋하지 않음
- 서버에만 안전하게 보관
- 환경 변수로 경로 관리

---

## 📌 관련 이슈
- AOIF-51: 푸시 알림 기능 구현

---

## 👥 리뷰어
@User팀원 - User.java fcmToken 필드 확인 부탁드립니다.
@앱팀원 - Firebase 앱 등록 및 FCM 토큰 연동 부탁드립니다.

