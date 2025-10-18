# 🎨 감정 기록 플로우 구현 가이드

## 📋 목차
1. [플로우 개요](#플로우-개요)
2. [수정된 파일 목록](#수정된-파일-목록)
3. [API 엔드포인트](#api-엔드포인트)
4. [데이터베이스 변경사항](#데이터베이스-변경사항)
5. [플로우 상세 설명](#플로우-상세-설명)
6. [사용 예시](#사용-예시)

---

## 📱 플로우 개요

### 전체 흐름
```
1. 사용자가 그라디언트 맵에서 색상 선택 (Main + Sub)
   ↓
2. 선택한 색상/좌표 정보를 서버로 전송
   ↓
3. 서버가 4-5개의 감정 추천
   ↓
4. 사용자가 추천된 감정 중 하나 선택
   ↓
5. 감정 기록 작성 화면으로 이동
   - 선택한 색상 표시
   - 선택한 감정 표시
   - 텍스트 입력
   - 공유 여부 선택
   ↓
6. 완료 버튼으로 저장
```

---

## 📂 수정된 파일 목록

### ✅ 수정된 기존 파일

#### 1. **EmotionRecord.java** (도메인)
- ✨ 추가된 필드:
  - `mainColor` - Main 색상 (HEX 코드)
  - `subColor` - Sub 색상 (HEX 코드)
  - `positionX` - X축 좌표
  - `positionY` - Y축 좌표
  - `emotionName` - 사용자가 선택한 구체적인 감정명

- 🔧 수정된 메서드:
  - `update()` - 전체 필드 업데이트
  - `updateRecord()` - 텍스트만 간단 업데이트

#### 2. **EmotionCreateRequest.java** (DTO)
- ✨ 추가된 필드:
  - `emotion_name` - 구체적인 감정명 (@NotBlank)
  - `main_color` - Main 색상
  - `sub_color` - Sub 색상
  - `position_x` - X 좌표
  - `position_y` - Y 좌표
  - `is_shared` - 공유 여부 (@NotNull)

#### 3. **EmotionResponse.java** (DTO)
- ✨ 추가된 필드: 색상, 좌표, 감정명 정보

#### 4. **EmotionListResponse.java** (DTO)
- ✨ 추가된 필드: 감정명, 색상 정보

#### 5. **EmotionDeleteResponse.java** (DTO)
- 🔧 수정: `from()` → `of()` 메서드로 변경 (is_deleted 필드 문제 해결)

#### 6. **EmotionRecordService.java**
- 🔧 `createEmotionRecord()`: 새 필드들 저장
- 🔧 `updateEmotionRecord()`: updateRecord() 사용
- 🔧 `deleteEmotionRecord()`: EmotionDeleteResponse.of() 사용

#### 7. **EmotionRecordController.java**
- ✨ 추가된 API: `POST /api/v1/emotion/recommend`
- 🔧 의존성 추가: `EmotionRecommendService`

---

### 🆕 새로 생성된 파일

#### 1. **EmotionRecommendRequest.java** (DTO)
```java
// 색상 선택 시 서버로 전송
{
  "main_color": "#FF5733",
  "sub_color": "#3357FF",
  "position_x": 0.8,
  "position_y": 0.2
}
```

#### 2. **EmotionRecommendResponse.java** (DTO)
```java
// 추천된 감정 목록
{
  "suggestions": [
    {
      "emotion_name": "만족스러움",
      "emoji_emotion": "HAPPY",
      "description": null
    },
    {
      "emotion_name": "행복함",
      "emoji_emotion": "HAPPY",
      "description": null
    }
    // ... 4-5개
  ]
}
```

#### 3. **EmotionRecommendService.java**
- 색상/좌표 기반 감정 추천 로직
- 10x10 그리드로 영역 분할
- 각 영역별로 4-5개 감정 매핑

---

## 🔌 API 엔드포인트

### 1️⃣ 감정 추천 API (신규)

```http
POST /api/v1/emotion/recommend
Content-Type: application/json

Request Body:
{
  "main_color": "#FF5733",
  "sub_color": "#3357FF",
  "position_x": 0.8,
  "position_y": 0.2
}

Response (200 OK):
{
  "code": 200,
  "message": "감정 추천 완료",
  "data": {
    "suggestions": [
      {
        "emotion_name": "만족스러움",
        "emoji_emotion": "HAPPY",
        "description": null
      },
      {
        "emotion_name": "행복함",
        "emoji_emotion": "HAPPY",
        "description": null
      },
      {
        "emotion_name": "여유로움",
        "emoji_emotion": "CALM",
        "description": null
      },
      {
        "emotion_name": "포근함",
        "emoji_emotion": "CALM",
        "description": null
      }
    ]
  }
}
```

---

### 2️⃣ 감정 기록 생성 API (수정됨)

```http
POST /api/v1/emotion
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "record": "오늘은 정말 행복한 하루였어요!",
  "emoji_emotion": "HAPPY",
  "emotion_name": "만족스러움",  // ✨ 신규
  "main_color": "#FF5733",      // ✨ 신규
  "sub_color": "#3357FF",       // ✨ 신규
  "position_x": 0.8,            // ✨ 신규
  "position_y": 0.2,            // ✨ 신규
  "is_shared": true             // ✨ 필수 (기존: 자동 false)
}

Response (201 Created):
{
  "code": 201,
  "message": "감정기록이 성공적으로 저장되었습니다.",
  "data": {
    "record_id": 1,
    "user_id": 1,
    "record": "오늘은 정말 행복한 하루였어요!",
    "emoji_emotion": "HAPPY",
    "emotion_name": "만족스러움",
    "main_color": "#FF5733",
    "sub_color": "#3357FF",
    "position_x": 0.8,
    "position_y": 0.2,
    "is_shared": true,
    "created_at": "2024-01-15T10:30:00",
    "updated_at": "2024-01-15T10:30:00"
  }
}
```

---

### 3️⃣ 감정 기록 조회 API (수정됨)

기존 API들(`GET /emotion`, `GET /emotion/{id}` 등)의 응답에 새 필드들이 포함됩니다.

```json
{
  "record_id": 1,
  "record": "...",
  "emoji_emotion": "HAPPY",
  "emotion_name": "만족스러움",     // ✨ 신규
  "main_color": "#FF5733",         // ✨ 신규
  "sub_color": "#3357FF",          // ✨ 신규
  "position_x": 0.8,               // ✨ 신규
  "position_y": 0.2,               // ✨ 신규
  "is_shared": true,
  "created_at": "..."
}
```

---

## 🗄️ 데이터베이스 변경사항

### emotion_record 테이블 스키마 변경

```sql
ALTER TABLE emotion_record 
ADD COLUMN main_color VARCHAR(7),          -- Main 색상 (HEX)
ADD COLUMN sub_color VARCHAR(7),           -- Sub 색상 (HEX)
ADD COLUMN position_x DOUBLE,              -- X 좌표
ADD COLUMN position_y DOUBLE,              -- Y 좌표
ADD COLUMN emotion_name VARCHAR(50) NOT NULL; -- 구체적인 감정명
```

### 변경된 테이블 구조

```
emotion_record
├── record_id (BIGINT, PK, AUTO_INCREMENT)
├── user_id (BIGINT, NOT NULL)
├── record (TEXT, NOT NULL)
├── emoji_emotion (VARCHAR, NOT NULL)      -- 카테고리 (HAPPY, SAD 등)
├── emotion_name (VARCHAR(50), NOT NULL)   -- ✨ 신규: 구체적인 감정명
├── main_color (VARCHAR(7))                -- ✨ 신규: Main 색상
├── sub_color (VARCHAR(7))                 -- ✨ 신규: Sub 색상
├── position_x (DOUBLE)                    -- ✨ 신규: X 좌표
├── position_y (DOUBLE)                    -- ✨ 신규: Y 좌표
├── is_shared (BOOLEAN, NOT NULL, DEFAULT false)
├── created_at (DATETIME, NOT NULL)
└── updated_at (DATETIME, NOT NULL)
```

---

## 🔄 플로우 상세 설명

### Step 1: 색상 선택
**화면**: 그라디언트 맵
**동작**: 사용자가 손가락으로 화면을 쓸어 색상 선택
**데이터**: 
- Main Color (HEX)
- Sub Color (HEX)
- Position X (0.0 ~ 1.0)
- Position Y (0.0 ~ 1.0)

---

### Step 2: 감정 추천 요청
**API**: `POST /api/v1/emotion/recommend`

**프론트엔드 코드 예시**:
```javascript
// 색상 선택 완료 시
async function onColorSelected(mainColor, subColor, positionX, positionY) {
  const response = await fetch('/api/v1/emotion/recommend', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      main_color: mainColor,
      sub_color: subColor,
      position_x: positionX,
      position_y: positionY
    })
  });
  
  const result = await response.json();
  // result.data.suggestions: 추천된 감정 목록 (4-5개)
  showEmotionOptions(result.data.suggestions);
}
```

---

### Step 3: 감정 선택
**화면**: 감정 선택 화면
**표시**: 4-5개의 추천 감정 버튼
**동작**: 사용자가 하나 선택

**UI 예시**:
```
[행복함]  [만족스러움]  [여유로움]  [포근함]
```

---

### Step 4: 감정 기록 작성
**화면**: 감정 기록 작성 화면
**표시**:
- 선택한 색상 (그라디언트)
- 선택한 감정명 ("만족스러움")
- 텍스트 입력창
- 친구 공개 토글 스위치

---

### Step 5: 저장
**API**: `POST /api/v1/emotion`

**프론트엔드 코드 예시**:
```javascript
async function saveEmotionRecord(recordText, isShared) {
  const response = await fetch('/api/v1/emotion', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      record: recordText,
      emoji_emotion: selectedEmotion.emoji_emotion,
      emotion_name: selectedEmotion.emotion_name,
      main_color: savedMainColor,
      sub_color: savedSubColor,
      position_x: savedPositionX,
      position_y: savedPositionY,
      is_shared: isShared
    })
  });
  
  const result = await response.json();
  // 저장 완료
  navigateToHome();
}
```

---

## 📱 사용 예시 (전체 플로우)

### 1. 색상 선택
```javascript
// 사용자가 그라디언트 맵 터치
const touchPosition = { x: 320, y: 150 }; // 픽셀 좌표
const mapSize = { width: 400, height: 800 };

// 정규화 (0.0 ~ 1.0)
const normalizedX = touchPosition.x / mapSize.width;  // 0.8
const normalizedY = touchPosition.y / mapSize.height; // 0.1875

// 색상 추출
const mainColor = getColorAtPosition(touchPosition); // "#FF5733"
const subColor = calculateSubColor(mainColor);       // "#3357FF"
```

---

### 2. 감정 추천 받기
```javascript
const recommendResponse = await fetch('/api/v1/emotion/recommend', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    main_color: "#FF5733",
    sub_color: "#3357FF",
    position_x: 0.8,
    position_y: 0.1875
  })
});

const { data } = await recommendResponse.json();
// data.suggestions: 
// [
//   { emotion_name: "만족스러움", emoji_emotion: "HAPPY" },
//   { emotion_name: "행복함", emoji_emotion: "HAPPY" },
//   { emotion_name: "여유로움", emoji_emotion: "CALM" },
//   { emotion_name: "포근함", emoji_emotion: "CALM" }
// ]
```

---

### 3. 감정 선택
```javascript
const selectedEmotion = data.suggestions[0]; // "만족스러움"
```

---

### 4. 감정 기록 작성 및 저장
```javascript
const recordText = "오늘은 정말 행복한 하루였어요!";
const isShared = true;

const createResponse = await fetch('/api/v1/emotion', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer eyJhbGc...',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    record: recordText,
    emoji_emotion: "HAPPY",
    emotion_name: "만족스러움",
    main_color: "#FF5733",
    sub_color: "#3357FF",
    position_x: 0.8,
    position_y: 0.1875,
    is_shared: isShared
  })
});

const { data } = await createResponse.json();
console.log("저장 완료!", data.record_id);
```

---

## ⚠️ 주의사항

### 1. 좌표 정규화
- 프론트엔드에서 좌표를 **0.0 ~ 1.0 범위로 정규화**하여 전송
- 다양한 화면 크기에 대응 가능

### 2. 색상 형식
- HEX 코드 형식 사용: `#RRGGBB` (7자리)
- 대소문자 구분 없음

### 3. 감정 카테고리 vs 감정명
- `emoji_emotion`: 카테고리 (HAPPY, SAD 등) - Enum
- `emotion_name`: 구체적인 감정명 (만족스러움, 행복함 등) - String
- **둘 다 필수 입력**

### 4. 공유 여부
- `is_shared`는 이제 **필수 입력 필드**
- 기본값 자동 설정 안 됨 (사용자가 명시적으로 선택)

---

## 🔧 감정 매핑 로직 커스터마이징

`EmotionRecommendService.java`의 `getEmotionsByGrid()` 메서드를 수정하여 표 이미지에 정확히 맞게 감정을 매핑할 수 있습니다.

```java
// 현재는 10x10 그리드 사용
// 표 이미지를 참고하여 각 영역별로 감정 매핑
```

---

## ✅ 마이그레이션 체크리스트

- [ ] 데이터베이스 스키마 업데이트 (ALTER TABLE)
- [ ] 기존 감정 기록에 기본값 설정 (NULL 허용 또는 기본값)
- [ ] 프론트엔드 API 연동 수정
- [ ] 감정 매핑 로직 정교화 (표 참고)
- [ ] 테스트 코드 작성

---

## 🎉 완료!

이제 전체 플로우가 구현되었습니다!

**핵심 변경사항**:
1. ✅ 색상 정보 저장 (Main, Sub)
2. ✅ 좌표 정보 저장
3. ✅ 구체적인 감정명 저장
4. ✅ 감정 추천 API 추가
5. ✅ 공유 여부 필수 입력

**추가 개선 가능**:
- 감정 매핑 테이블 DB화
- 감정 추천 로직 고도화 (ML/AI)
- 색상 분석 고도화

