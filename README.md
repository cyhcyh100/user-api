# User API

Kotlin과 Spring Boot로 구현한 RESTful API 서비스로, 회원가입과 인증 및 역할 기반 User API 를 구현합니다.

## 기능

- `POST /signup`: 사용자 가입 (이메일, 비밀번호 등 필수 정보 저장)
- `POST /signin`: 사용자 로그인 (이메일, 비밀번호로 인증 후 토큰 발급 또는 세션 관리)
- `DELETE /users/{userId}`: 특정 사용자 정보 삭제 (탈퇴)
- `PUT /users/{userId}`: 특정 사용자 정보 변경 (email, name)
- `GET /users/{userId}`: 특정 사용자 정보 조회
- `GET /users`: 모든 사용자 정보 조회 (Admin 만 사용 가능, 페이징 처리)

```md
참고: 별도의 Admin 계정을 생성하는 API는 제공하지 않으며,
애플리케이션이 처음 기동될 때 아래와 같은 정보를 가진 기본 Admin 계정이 자동으로 등록됩니다.

- email: admin@example.com
- password: password
- name: admin
- role: ADMIN
```

## 기술 스택

- Kotlin 1.9 & Spring Boot 3.5
- Spring MVC, Spring Data JPA, Spring Security
- PostgreSQL
- JSON Web Token (JJWT)
- RabbitMQ

## 설계 결정 이유

- **Spring Security + JWT**: OAuth2 기반 인증을 위해 선택
- **레이어드 아키텍처**: 도메인 로직, 비즈니스 로직, API 응답을 명확히 분리하여 유지보수성을 확보
- **RabbitMQ**: 이벤트 기반 비동기 처리에서 Kafka보다 설정도 단순하고 무겁지 않은 RabbitMQ 선택

### 비동기 처리

- 사용자가 삭제될 때, 이메일 발송이나 사용자 파일 제거와 같은 관련 정리 작업은 비동기로 처리
- RabbitMQ 를 사용하여 event 를 발행하며, 이를 통해 비동기 작업 수행

## 앱 시작하기

### 필수 조건

- JDK 21 이상
- Docker & Docker Compose

### 전체 앱 실행

```bash
docker compose -f docker/docker-compose.yml up -d
```

### 테스트

- **테스트 커버리지** : Class(96%), Method(95%), Line(97%)
- 테스트 실행:

```
./gradlew test
```
