# AI 피드백 시스템

## 🚀 빠른 시작

### 1. 환경변수 설정
```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="your-openai-api-key"

# Windows (CMD)
set OPENAI_API_KEY=your-openai-api-key

# Linux/Mac
export OPENAI_API_KEY="your-openai-api-key"
```

### 2. 서버 실행
```bash
./gradlew bootRun
```

### 3. 테스트
- 브라우저: `http://localhost:8080/test.html`
- API: POST `/api/v1/feedback` (recordId: 1, 2, 3)

## 🔑 OpenAI API 키 발급
1. https://platform.openai.com/api-keys 접속
2. "Create new secret key" 클릭
3. 키 복사 후 환경변수에 설정

## ⚠️ 주의사항
- API 키는 절대 Git에 커밋하지 마세요!
- 테스트용 샘플 데이터가 포함되어 있습니다.