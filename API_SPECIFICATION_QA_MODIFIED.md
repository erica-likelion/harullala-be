# QA 수정사항 API 명세서

## 1. 모든 알림 읽음 처리

### 개요

- **API 이름**: 모든 알림 읽음 처리
- **설명**: 사용자의 모든 읽지 않은 알림을 한 번에 읽음 처리합니다.

---

### 요청(Request)

- **HTTP Method**: `PUT`
- **URL**: `/api/v1/notifications/read-all`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {access_token}`
- **Request Body**: 없음

---

### 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "모든 알림을 읽음 처리했습니다.",
  "data": null
}
```

---

### 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | `int` | ✅ Yes | 결과 코드 |
| message | `string` | ✅ Yes | 응답 메시지 |
| data | `null` | ✅ Yes | 응답 데이터 |

---

### 에러 코드 및 응답 형식

서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

- 필드 설명:
  - 401: 토큰 검증 실패 (인증 필요)
  - 500: 내부 오류

---

### 📝 비고

- 모든 읽지 않은 알림이 `isRead: true`로 변경되고 `readAt`에 현재 시간이 기록됩니다.

---

## 2. 알림 목록 조회

### 개요

- **API 이름**: 알림 목록 조회
- **설명**: 페이징, 날짜 필터로 조회

---

### 요청(Request)

- **HTTP Method**: `GET`
- **URL**: `/api/v1/notifications`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {access_token}`
- **Query Parameters**:
  - `page`: 페이지 번호 (예: `1`)
  - `size`: 페이지 크기 (예: `20`)
  - `days`: 최근 N일 필터 (선택, 예: `7`, `30`)
- **Request Body**: 없음

---

### 요청 필드 상세 (Request Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| page | `integer` | No | 페이지번호(기본값 0) |
| size | `integer` | No | 페이지당 항목 수(기본값 : 20) |
| days | `integer` | No | 최근 N일 필터(7일, 30일) |

---

### 응답(Response)

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

### 응답 필드 상세 (Response Body)

**data.notifications 배열**

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| notificationId | `Long` | ✅ Yes | 알림ID |
| type | `String` | ✅ Yes | 알림타입(AI_FEEDBACK, FRIEND_REQUEST, FRIEND_ACCEPTED, FRIEND_EMOTION_RECORD) |
| title | `String` | ✅ Yes | 알림 제목 |
| message | `String` | ✅ Yes | 알림 내용 |
| relatedId | `Long` | No | 관련 엔티티 ID(feedbackId, friendRelationshipId, recordId) |
| isRead | `Boolean` | ✅ Yes | 읽음 여부 |
| readAt | `String` | No | 읽은 시간(ISO8601형식, NULL이면 안읽음) |
| createdAt | `String` | ✅ Yes | 알림 생성시간(ISO 8601형식) |

**data 페이징 정보**

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| currentPage | `Integer` | ✅ Yes | 현재 페이지 번호 |
| totalPages | `Integer` | ✅ Yes | 전체 페이지 수 |
| totalElements | `Long` | ✅ Yes | 전체 알림 개수 |
| hasNext | `Boolean` | ✅ Yes | 다음 페이지 존재여부 |

---

### 에러 코드 및 응답 형식

서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

- 필드 설명:
  - 400: 필수 값 누락/형식 오류
  - 401: 토큰 검증 실패 (인증 필요)
  - 500: 내부 오류

---

### 📝 비고

- 알림 타입별 relatedId 매핑

| type | relatedId 가리키는 것 | 화면 이동 |
| --- | --- | --- |
| AI_FEEDBACK | recordId | 피드백받은 감정기록 |
| FRIEND_REQUEST | friendRelationshipId | 친구 요청 화면 |
| FRIEND_ACCEPTED | friendRelationshipId | 친구 목록 화면 |
| FRIEND_EMOTION_RECORD | recordId | 친구 감정 기록 상세 화면 |

- 사용 예시
  - 기본 조회: `/api/v1/notifications`
  - 최근 7일: `/api/v1/notifications?days=7`
  - 최근 30일: `/api/v1/notifications?days=30`
  - 페이징: `/api/v1/notifications?page=1&size=10`

- **FCM 푸시 알림 관련**: FCM 푸시 알림의 `data` 필드에 `notificationId`가 **새로 추가**되었습니다. 클라이언트에서 푸시 알림을 받았을 때 `data.notificationId`를 사용하여 읽음 처리 API(`PUT /api/v1/notifications/{notificationId}/read`)를 바로 호출할 수 있습니다.

---

## 3. FCM 푸시 알림 Data 필드 추가 (✅ 새로 추가)

### 개요

- **API 이름**: FCM 푸시 알림
- **설명**: FCM 푸시 알림의 `data` 필드에 `notificationId`가 **새로 추가**되었습니다. 클라이언트에서 푸시 알림을 받았을 때 `notificationId`를 사용하여 읽음 처리할 수 있습니다.

---

### 푸시 알림 Data 구조

```json
{
  "type": "AI_FEEDBACK",
  "title": "AI 피드백이 도착했어요",
  "message": "오늘의 감정에 대한 AI 피드백을 확인해보세요",
  "notificationId": "123",
  "relatedId": "456"
}
```

---

### Data 필드 상세

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| type | `string` | ✅ Yes | 알림 타입 |
| title | `string` | ✅ Yes | 알림 제목 |
| message | `string` | ✅ Yes | 알림 메시지 |
| notificationId | `string` | ✅ Yes | 알림 ID (**✅ 새로 추가됨**) |
| relatedId | `string` | ❌ No | 관련 엔티티 ID |

---

### 📝 비고

- **이번에 새로 추가된 기능입니다.**
- 클라이언트에서 푸시 알림을 받으면 `data.notificationId`를 사용하여 읽음 처리 API를 호출할 수 있습니다.
- 읽음 처리 API: `PUT /api/v1/notifications/{notificationId}/read`
- 이전에는 푸시 알림에 `notificationId`가 없어서, 알림 목록을 조회한 후 `notificationId`를 찾아야 했지만, 이제는 푸시 알림에서 바로 읽음 처리가 가능합니다.

---

## 4. AI 피드백 생성 (동작 변경)

### 개요

- **API 이름**: AI 피드백 생성/재생성
- **설명**: 감정 기록에 대한 AI 피드백을 생성합니다. 요청 시 즉시 "처리 중" 응답을 반환하고, 백그라운드에서 비동기로 AI 답변을 생성합니다. 완료 시 SSE 이벤트와 푸시 알림이 전송됩니다.

---

### 요청(Request)

- **HTTP Method**: `POST`
- **URL**: `/api/v1/feedback`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {access_token}`
- **Request Body**:

```json
{
  "recordId": 123
}
```

---

### 요청 필드 상세 (Request Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| recordId | `Long` | ✅ Yes | 감정 기록 ID |

---

### 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "AI 피드백 생성 성공",
  "data": {
    "feedbackId": null,
    "recordId": 123,
    "aiReply": null,
    "attemptsUsed": 1,
    "createdAt": null,
    "updatedAt": null
  }
}
```

---

### 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | `int` | ✅ Yes | 결과 코드 |
| message | `string` | ✅ Yes | 응답 메시지 |
| data | `object` | ✅ Yes | 응답 데이터 |
| data.feedbackId | `Long` | ❌ No | 피드백 ID (백그라운드 처리 중) |
| data.recordId | `Long` | ✅ Yes | 감정 기록 ID |
| data.aiReply | `string` | ❌ No | AI 답변 (백그라운드 처리 중) |
| data.attemptsUsed | `int` | ✅ Yes | 사용한 시도 횟수 |
| data.createdAt | `string` | ❌ No | 생성 시간 (백그라운드 처리 중) |
| data.updatedAt | `string` | ❌ No | 수정 시간 (백그라운드 처리 중) |

---

### 에러 코드 및 응답 형식

서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 404,
  "message": "Record not found",
  "data": null
}
```

- 필드 설명:
  - 401: 토큰 검증 실패 (인증 필요)
  - 403: 해당 기록에 접근할 권한이 없음
  - 404: 기록을 찾을 수 없음
  - 409: 피드백 생성 횟수 초과 (3/3)
  - 500: 내부 오류

---

### 📝 비고

- **동작 방식**: `POST /api/v1/feedback` 요청 시 즉시 "처리 중" 응답을 반환하고, 백엔드에서 1분 후에 AI 답변을 생성하여 DB에 저장하고 푸시 알림을 전송합니다.
- **프론트엔드 조회 방식**:
  - **방법 1 (1분 후 자동 조회 - 권장)**: `POST /api/v1/feedback` 요청 후 1분이 지나면 자동으로 `GET /api/v1/feedback?recordId={recordId}` API를 호출하여 답변을 조회합니다.
  - **방법 2 (푸시 알림 활용)**: 사용자가 앱 밖에 있을 때, 푸시 알림을 받으면 알림을 눌러서 앱으로 들어가면 그때 `GET /api/v1/feedback?recordId={recordId}` API를 호출하여 답변을 조회합니다.
  - **방법 3 (수동 조회)**: 사용자가 직접 "AI 피드백 보기" 버튼을 눌러서 조회
- 최대 3번까지 재생성 가능합니다.

---

## 5. 친구 피드 목록 조회 (응답 변경)

### 개요

- **API 이름**: 친구 피드 목록 조회
- **설명**: 친구들의 공유된 감정 기록 피드를 조회합니다. **내가 작성한 감정기록도 목록에 포함됩니다.**

---

### 요청(Request)

- **HTTP Method**: `GET`
- **URL**: `/api/v1/friend-feed`
- **Headers**:
  - `Authorization`: `Bearer {access_token}`
- **Query Parameters**:
  - `page`: 페이지 번호 (기본값: `1`)
  - `size`: 페이지 크기 (기본값: `10`)

---

### 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "친구 피드 조회 성공",
  "data": [
    {
      "recordId": 123,
      "userId": 1,
      "nickname": "홍길동",
      "profileImageUrl": "https://example.com/profile.jpg",
      "record": "오늘 기분이 좋아요",
      "emotionName": "기쁨",
      "mainColor": "#FFD700",
      "subColor": "#FFA500",
      "textColor": "#000000",
      "isShared": true,
      "isRead": false,
      "readCount": 0,
      "createdAt": "2025-11-26T15:30:00"
    }
  ]
}
```

---

### 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | `int` | ✅ Yes | 결과 코드 |
| message | `string` | ✅ Yes | 응답 메시지 |
| data | `array` | ✅ Yes | 피드 목록 |
| data[].recordId | `Long` | ✅ Yes | 감정 기록 ID |
| data[].userId | `Long` | ✅ Yes | 작성자 ID (내 ID일 수도 있음) |
| data[].nickname | `string` | ✅ Yes | 작성자 닉네임 |
| data[].profileImageUrl | `string` | ❌ No | 작성자 프로필 이미지 URL |
| data[].record | `string` | ✅ Yes | 감정 기록 내용 |
| data[].emotionName | `string` | ✅ Yes | 감정명 |
| data[].mainColor | `string` | ✅ Yes | 메인 색상 |
| data[].subColor | `string` | ✅ Yes | 서브 색상 |
| data[].textColor | `string` | ✅ Yes | 텍스트 색상 |
| data[].isShared | `boolean` | ✅ Yes | 공유 여부 |
| data[].isRead | `boolean` | ✅ Yes | 읽음 여부 |
| data[].readCount | `long` | ✅ Yes | 읽은 사람 수 |
| data[].createdAt | `string` | ✅ Yes | 생성 시간 (ISO8601) |

---

### 에러 코드 및 응답 형식

서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

- 필드 설명:
  - 401: 토큰 검증 실패 (인증 필요)
  - 500: 내부 오류

---

### 📝 비고

- **변경사항**: 내가 작성한 감정기록도 피드 목록에 포함됩니다.
- 친구들의 기록과 내 기록이 함께 최신순으로 정렬되어 표시됩니다.
- 24시간 이내의 공유된 기록만 조회됩니다.
- 내 기록인 경우 `userId`가 현재 사용자의 ID와 동일합니다.

---

## 6. 친구 피드 상세 조회 (권한 변경)

### 개요

- **API 이름**: 친구 피드 상세 조회
- **설명**: 특정 감정 기록의 상세 정보를 조회합니다. **내가 작성한 기록도 조회 가능합니다.**

---

### 요청(Request)

- **HTTP Method**: `GET`
- **URL**: `/api/v1/friend-feed/{recordId}`
- **Headers**:
  - `Authorization`: `Bearer {access_token}`
- **Path Parameters**:
  - `recordId`: 감정 기록 ID

---

### 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "친구 피드 상세 조회 성공",
  "data": {
    "recordId": 123,
    "userId": 1,
    "nickname": "홍길동",
    "profileImageUrl": "https://example.com/profile.jpg",
    "record": "오늘 기분이 좋아요",
    "emotionName": "기쁨",
    "mainColor": "#FFD700",
    "subColor": "#FFA500",
    "textColor": "#000000",
    "isShared": true,
    "isRead": false,
    "readCount": 0,
    "createdAt": "2025-11-26T15:30:00"
  }
}
```

---

### 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | `int` | ✅ Yes | 결과 코드 |
| message | `string` | ✅ Yes | 응답 메시지 |
| data | `object` | ✅ Yes | 피드 상세 정보 |
| data.recordId | `Long` | ✅ Yes | 감정 기록 ID |
| data.userId | `Long` | ✅ Yes | 작성자 ID |
| data.nickname | `string` | ✅ Yes | 작성자 닉네임 |
| data.profileImageUrl | `string` | ❌ No | 작성자 프로필 이미지 URL |
| data.record | `string` | ✅ Yes | 감정 기록 내용 |
| data.emotionName | `string` | ✅ Yes | 감정명 |
| data.mainColor | `string` | ✅ Yes | 메인 색상 |
| data.subColor | `string` | ✅ Yes | 서브 색상 |
| data.textColor | `string` | ✅ Yes | 텍스트 색상 |
| data.isShared | `boolean` | ✅ Yes | 공유 여부 |
| data.isRead | `boolean` | ✅ Yes | 읽음 여부 |
| data.readCount | `long` | ✅ Yes | 읽은 사람 수 |
| data.createdAt | `string` | ✅ Yes | 생성 시간 (ISO8601) |

---

### 에러 코드 및 응답 형식

서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 403,
  "message": "친구의 기록만 조회할 수 있습니다.",
  "data": null
}
```

- 필드 설명:
  - 401: 토큰 검증 실패 (인증 필요)
  - 403: 친구의 기록이 아니거나 공유되지 않은 기록
  - 404: 기록을 찾을 수 없음
  - 500: 내부 오류

---

### 📝 비고

- **변경사항**: 내가 작성한 기록도 조회 가능합니다.
- 공유된 기록(`isShared: true`)이어야 합니다.
- 친구의 기록이거나 내 기록이어야 조회 가능합니다.

---

## 7. 친구 피드 읽음 처리 (권한 변경)

### 개요

- **API 이름**: 친구 피드 읽음 처리
- **설명**: 특정 감정 기록을 읽음 처리합니다. **내가 작성한 기록도 읽음 처리 가능합니다.**

---

### 요청(Request)

- **HTTP Method**: `POST`
- **URL**: `/api/v1/friend-feed/read`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {access_token}`
- **Request Body**:

```json
{
  "recordId": 123
}
```

---

### 요청 필드 상세 (Request Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| recordId | `Long` | ✅ Yes | 읽음 처리할 감정 기록 ID |

---

### 응답(Response)

- **Status Code**: `200 OK`
- **Response Body**:

```json
{
  "code": 200,
  "message": "피드 읽음 처리 완료",
  "data": null
}
```

---

### 응답 필드 상세 (Response Body)

| 필드명 | 타입 | 필수 여부 | 설명 |
| --- | --- | --- | --- |
| code | `int` | ✅ Yes | 결과 코드 |
| message | `string` | ✅ Yes | 응답 메시지 |
| data | `null` | ✅ Yes | 응답 데이터 |

---

### 에러 코드 및 응답 형식

서버에서 에러 발생 시, 아래와 같은 공통 포맷으로 에러 응답을 반환합니다.

```json
{
  "code": 403,
  "message": "친구의 기록만 읽을 수 있습니다.",
  "data": null
}
```

- 필드 설명:
  - 401: 토큰 검증 실패 (인증 필요)
  - 403: 친구의 기록이 아니거나 공유되지 않은 기록
  - 404: 기록을 찾을 수 없음
  - 500: 내부 오류

---

### 📝 비고

- **변경사항**: 내가 작성한 기록도 읽음 처리 가능합니다.
- 공유된 기록(`isShared: true`)이어야 합니다.
- 친구의 기록이거나 내 기록이어야 읽음 처리 가능합니다.
- 이미 읽은 기록인 경우 중복 처리되지 않습니다.
