# 하룰랄라 백엔드

---
## 프로젝트 구조

```text
harullala/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── likelion/harullala/
│   │   │       ├── HarullalaApplication.java
│   │   │       ├── config/         
│   │   │       ├── controller/     
│   │   │       ├── domain/         
│   │   │       ├── dto/            
│   │   │       ├── exception/      
│   │   │       ├── repository/     
│   │   │       └── service/        
│   │   └── resources/              
│   └── test/                     
├── .github/                      
├── scripts/                      
├── build.gradle                     
└── README.md
```
---

## 🛠️ 기술 스택

| 분야     | 스택                |
|--------|-------------------|
| 언어     | Java 17           |
| 프레임워크  | Spring Boot 3.5.4 |
| 데이터베이스 | MySQL 8.0         |
| 빌드 도구  | Gradle 8.9        |
| ORM    | Spring Data JPA   |
| 배포     | AWS 예정            |

---
## Commit Convection
```
feat: 새로운 기능 추가

fix: 버그 수정

docs: 문서 수정

style: 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우

refactor: 코드 리팩토링

test: 테스트 코드, 리팩토링 테스트 코드 추가

chore: 빌드 업무 수정, 패키지 매니저 수정, production code와 무관한 부분들

comment: 주석 추가 및 변경

remove: 파일, 폴더 삭제

rename: 파일, 폴더명 수정
```
---