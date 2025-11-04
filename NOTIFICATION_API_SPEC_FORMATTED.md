# 알림 API 명세서

---

# 1. FCM 토큰 등록 API

## 개요

- **API 이름**: FCM 토큰 등록
- **설명**: 사용자의 FCM 토큰을 서버에 등록하여 푸시 알림을 받을 수 있도록 합니다.

---

## 요청(Request)

- **HTTP Method**: `POST`
- **URL**: `/api/v1/notifications/token`
- **Headers**:
    - `Content-Type`: `application/json`
    - `Authorization`: `Bearer {access_token}`
- **Query Parameters**: 없음
- **Request Body**:

```json
{
  "fcmToken": "cXYz123ABc:APA91bH...매우긴토큰문자열...xyz789"
}
```

---

## 요청 필드 상세 (Request Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| fcmToken | String | ✅ Yes | Firebase에서 발급받은 FCM 토큰 |

---

## 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "FCM 토큰이 등록되었습니다.",
  "data": null
}
```

---

## 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | Integer | ✅ Yes | 응답 코드 |
| message | String | ✅ Yes | 응답 메시지 |
| data | Object | ✅ Yes | 응답 데이터 (토큰 등록은 null) |

---

## 에러 코드 및 응답 형식

- 서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 400,
  "message": "FCM 토큰은 필수입니다.",
  "data": null
}
```

- 필드 설명:
    - `400`: 필수 값 누락/형식 오류
    - `401`: 토큰 검증 실패 (인증 필요)
    - `500`: 내부 오류

---

### 📝 비고
- 앱 실행 시 또는 로그인 시 호출
- FCM 토큰은 Firebase SDK에서 자동 발급
- 토큰이 변경될 때마다 재등록 필요

---

# 2. 알림 목록 조회 API

## 개요

- **API 이름**: 알림 목록 조회
- **설명**: 사용자의 알림 목록을 페이징 및 날짜 필터로 조회합니다.

---

## 요청(Request)

- **HTTP Method**: `GET`
- **URL**: `/api/v1/notifications`
- **Headers**:
    - `Authorization`: `Bearer {access_token}`
- **Query Parameters**:
    - `page`: 페이지 번호 (기본값: `0`, 0부터 시작)
    - `size`: 페이지 크기 (기본값: `20`)
    - `days`: 최근 N일 필터 (선택, 예: `7`, `30`)
- **Request Body**: 없음

---

## 요청 필드 상세 (Query Parameters)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| page | Integer | ❌ No | 페이지 번호 (0부터 시작, 기본값: 0) |
| size | Integer | ❌ No | 페이지당 항목 수 (기본값: 20) |
| days | Integer | ❌ No | 최근 N일 필터 (예: 7, 30) |

---

## 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "알림 목록을 조회했습니다.",
  "data": {
    "notifications": [
      {
        "notificationId": 1,
        "type": "AI_FEEDBACK",
        "title": "AI 피드백이 도착했어요",
        "message": "오늘의 감정에 대한 AI 피드백을 확인해보세요",
        "relatedId": 123,
        "isRead": false,
        "readAt": null,
        "createdAt": "2025-11-04T10:30:00"
      },
      {
        "notificationId": 2,
        "type": "FRIEND_REQUEST",
        "title": "새로운 친구 요청이 도착했어요",
        "message": "홍길동님이 친구 요청을 보냈어요",
        "relatedId": 45,
        "isRead": true,
        "readAt": "2025-11-04T11:00:00",
        "createdAt": "2025-11-04T10:00:00"
      },
      {
        "notificationId": 3,
        "type": "FRIEND_ACCEPTED",
        "title": "친구 요청이 수락되었어요",
        "message": "김철수님이 친구 요청을 수락했어요",
        "relatedId": 46,
        "isRead": false,
        "readAt": null,
        "createdAt": "2025-11-04T09:30:00"
      },
      {
        "notificationId": 4,
        "type": "FRIEND_EMOTION_RECORD",
        "title": "친구가 감정 기록을 작성했어요",
        "message": "이영희님이 오늘의 감정을 기록했어요",
        "relatedId": 789,
        "isRead": false,
        "readAt": null,
        "createdAt": "2025-11-04T09:00:00"
      }
    ],
    "currentPage": 0,
    "totalPages": 5,
    "totalElements": 100,
    "hasNext": true
  }
}
```

---

## 응답 필드 상세 (Response Body)

### data.notifications 배열

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| notificationId | Long | ✅ Yes | 알림 ID |
| type | String | ✅ Yes | 알림 타입 (AI_FEEDBACK, FRIEND_REQUEST, FRIEND_ACCEPTED, FRIEND_EMOTION_RECORD) |
| title | String | ✅ Yes | 알림 제목 |
| message | String | ✅ Yes | 알림 내용 |
| relatedId | Long | ❌ No | 관련 엔티티 ID (feedbackId, friendRelationshipId, recordId) |
| isRead | Boolean | ✅ Yes | 읽음 여부 |
| readAt | String | ❌ No | 읽은 시간 (ISO 8601 형식, null이면 안읽음) |
| createdAt | String | ✅ Yes | 알림 생성 시간 (ISO 8601 형식) |

### data 페이징 정보

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| currentPage | Integer | ✅ Yes | 현재 페이지 번호 |
| totalPages | Integer | ✅ Yes | 전체 페이지 수 |
| totalElements | Long | ✅ Yes | 전체 알림 개수 |
| hasNext | Boolean | ✅ Yes | 다음 페이지 존재 여부 |

---

## 에러 코드 및 응답 형식

- 서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

- 필드 설명:
    - `400`: 필수 값 누락/형식 오류
    - `401`: 토큰 검증 실패 (인증 필요)
    - `500`: 내부 오류

---

### 📝 비고

#### 알림 타입별 relatedId 매핑

| type | relatedId가 가리키는 것 | 화면 이동 |
| --- | --- | --- |
| AI_FEEDBACK | feedbackId | AI 피드백 상세 화면 |
| FRIEND_REQUEST | friendRelationshipId | 친구 요청 화면 |
| FRIEND_ACCEPTED | friendRelationshipId | 친구 목록 화면 |
| FRIEND_EMOTION_RECORD | recordId | 친구 감정 기록 상세 화면 |

#### 사용 예시
- 기본 조회: `/api/v1/notifications`
- 최근 7일: `/api/v1/notifications?days=7`
- 최근 30일: `/api/v1/notifications?days=30`
- 페이징: `/api/v1/notifications?page=1&size=10`

---

# 3. 안읽은 알림 개수 조회 API

## 개요

- **API 이름**: 안읽은 알림 개수 조회
- **설명**: 사용자의 안읽은 알림 개수를 조회합니다.

---

## 요청(Request)

- **HTTP Method**: `GET`
- **URL**: `/api/v1/notifications/unread-count`
- **Headers**:
    - `Authorization`: `Bearer {access_token}`
- **Query Parameters**: 없음
- **Request Body**: 없음

---

## 요청 필드 상세 (Request Body)

없음 (GET 요청)

---

## 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "안읽은 알림 개수를 조회했습니다.",
  "data": {
    "unreadCount": 5
  }
}
```

---

## 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | Integer | ✅ Yes | 응답 코드 |
| message | String | ✅ Yes | 응답 메시지 |
| data | Object | ✅ Yes | 응답 데이터 |
| data.unreadCount | Long | ✅ Yes | 안읽은 알림 개수 |

---

## 에러 코드 및 응답 형식

- 서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

- 필드 설명:
    - `401`: 토큰 검증 실패 (인증 필요)
    - `500`: 내부 오류

---

### 📝 비고
- 앱 시작 시 또는 주기적으로 호출하여 뱃지 표시
- 알림함 아이콘에 빨간 점 또는 숫자 뱃지 표시
- 푸시 알림 수신 후 호출하여 개수 업데이트

---

# 4. 알림 읽음 처리 API

## 개요

- **API 이름**: 알림 읽음 처리
- **설명**: 특정 알림을 읽음 상태로 변경합니다.

---

## 요청(Request)

- **HTTP Method**: `PUT`
- **URL**: `/api/v1/notifications/{notificationId}/read`
- **Headers**:
    - `Authorization`: `Bearer {access_token}`
- **Path Parameters**:
    - `notificationId`: 읽음 처리할 알림 ID (예: `1`)
- **Query Parameters**: 없음
- **Request Body**: 없음

---

## 요청 필드 상세 (Path Parameters)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| notificationId | Long | ✅ Yes | 읽음 처리할 알림 ID |

---

## 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "알림을 읽음 처리했습니다.",
  "data": null
}
```

---

## 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | Integer | ✅ Yes | 응답 코드 |
| message | String | ✅ Yes | 응답 메시지 |
| data | Object | ✅ Yes | 응답 데이터 (읽음 처리는 null) |

---

## 에러 코드 및 응답 형식

- 서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 404,
  "message": "알림을 찾을 수 없습니다.",
  "data": null
}
```

- 필드 설명:
    - `401`: 토큰 검증 실패 (인증 필요)
    - `403`: 해당 알림에 접근할 권한이 없음
    - `404`: 알림을 찾을 수 없음
    - `500`: 내부 오류

---

### 📝 비고
- 사용자가 알림을 클릭했을 때 호출
- 읽음 처리 후 `readAt` 필드에 현재 시간이 자동 기록됨
- 이미 읽은 알림을 다시 읽음 처리해도 성공 응답 반환
- 읽음 처리 후 해당 화면으로 이동 (type과 relatedId 활용)

---

# 부록: 푸시 알림 데이터 형식

## FCM 푸시 메시지 구조

앱에서 푸시 알림을 수신할 때 다음과 같은 데이터 구조로 전달됩니다:

```json
{
  "notification": {
    "title": "AI 피드백이 도착했어요",
    "body": "오늘의 감정에 대한 AI 피드백을 확인해보세요"
  },
  "data": {
    "type": "AI_FEEDBACK",
    "title": "AI 피드백이 도착했어요",
    "message": "오늘의 감정에 대한 AI 피드백을 확인해보세요",
    "relatedId": "123"
  }
}
```

## 푸시 데이터 필드 설명

| 필드명 | 타입 | 설명 |
| --- | --- | --- |
| notification.title | String | 푸시 알림 제목 |
| notification.body | String | 푸시 알림 본문 |
| data.type | String | 알림 타입 (AI_FEEDBACK, FRIEND_REQUEST, FRIEND_ACCEPTED, FRIEND_EMOTION_RECORD) |
| data.title | String | 알림 제목 |
| data.message | String | 알림 내용 |
| data.relatedId | String | 관련 엔티티 ID (문자열 형태) |

## Android 푸시 수신 처리 예시

```kotlin
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val data = remoteMessage.data
    val type = data["type"] // "AI_FEEDBACK"
    val title = data["title"]
    val message = data["message"]
    val relatedId = data["relatedId"]?.toLong()
    
    // 알림 표시
    showNotification(title, message)
    
    // 클릭 시 화면 이동 처리
    val intent = when (type) {
        "AI_FEEDBACK" -> Intent(this, FeedbackDetailActivity::class.java).apply {
            putExtra("feedbackId", relatedId)
        }
        "FRIEND_REQUEST" -> Intent(this, FriendRequestActivity::class.java).apply {
            putExtra("relationshipId", relatedId)
        }
        "FRIEND_ACCEPTED" -> Intent(this, FriendListActivity::class.java)
        "FRIEND_EMOTION_RECORD" -> Intent(this, EmotionRecordDetailActivity::class.java).apply {
            putExtra("recordId", relatedId)
        }
        else -> Intent(this, MainActivity::class.java)
    }
    
    // 읽음 처리 API 호출
    api.markAsRead(notificationId)
    
    startActivity(intent)
}
```

---

# 부록: 사용 흐름 예시

## 1. 앱 초기 설정 흐름

```
1. 앱 설치 및 실행
   ↓
2. Firebase에서 FCM 토큰 자동 발급
   ↓
3. POST /api/v1/notifications/token (토큰 등록)
   ↓
4. GET /api/v1/notifications/unread-count (안읽은 알림 개수 조회)
   ↓
5. 알림함 아이콘에 뱃지 표시
```

## 2. 푸시 알림 수신 흐름

```
1. 백엔드에서 이벤트 발생 (AI 피드백, 친구 요청 등)
   ↓
2. 백엔드가 DB에 알림 저장
   ↓
3. 백엔드가 FCM으로 푸시 전송
   ↓
4. 앱에서 푸시 알림 수신 및 표시
   ↓
5. 사용자가 푸시 알림 클릭
   ↓
6. PUT /api/v1/notifications/{id}/read (읽음 처리)
   ↓
7. type과 relatedId를 이용해 해당 화면으로 이동
```

## 3. 알림함 사용 흐름

```
1. 사용자가 알림함 아이콘 클릭
   ↓
2. GET /api/v1/notifications (알림 목록 조회)
   ↓
3. 알림 목록 표시 (최신순)
   ↓
4. 사용자가 특정 알림 선택
   ↓
5. PUT /api/v1/notifications/{id}/read (읽음 처리)
   ↓
6. type과 relatedId를 이용해 해당 화면으로 이동
   ↓
7. GET /api/v1/notifications/unread-count (안읽은 알림 개수 재조회)
   ↓
8. 뱃지 개수 업데이트
```

## 4. 주기적 업데이트 흐름

```
1. 앱이 포그라운드로 전환될 때
   ↓
2. GET /api/v1/notifications/unread-count (안읽은 알림 개수 조회)
   ↓
3. 뱃지 개수 업데이트
```

---

# 부록: 에러 코드 전체 목록

| 코드 | 설명 | 발생 상황 |
| --- | --- | --- |
| 200 | 성공 | 정상 처리 |
| 400 | 잘못된 요청 | 필수 값 누락, 형식 오류 |
| 401 | 인증 실패 | JWT 토큰 검증 실패, 토큰 만료 |
| 403 | 권한 없음 | 다른 사용자의 알림에 접근 시도 |
| 404 | 리소스 없음 | 알림 ID가 존재하지 않음 |
| 500 | 서버 에러 | 내부 서버 오류 |

---

# 부록: 테스트 시나리오

## 1. FCM 토큰 등록 테스트

```bash
curl -X POST https://api.harullala.com/api/v1/notifications/token \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"fcmToken": "test_token_123"}'
```

예상 응답: `200 OK`

## 2. 알림 목록 조회 테스트

```bash
# 전체 조회
curl -X GET "https://api.harullala.com/api/v1/notifications" \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 최근 7일
curl -X GET "https://api.harullala.com/api/v1/notifications?days=7" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

예상 응답: `200 OK`, 알림 목록 포함

## 3. 안읽은 알림 개수 조회 테스트

```bash
curl -X GET "https://api.harullala.com/api/v1/notifications/unread-count" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

예상 응답: `200 OK`, unreadCount 포함

## 4. 알림 읽음 처리 테스트

```bash
curl -X PUT "https://api.harullala.com/api/v1/notifications/1/read" \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

예상 응답: `200 OK`

---

# 변경 이력

| 버전 | 날짜 | 변경 내용 |
| --- | --- | --- |
| v1.0.0 | 2025-11-04 | 초기 API 구현 |

