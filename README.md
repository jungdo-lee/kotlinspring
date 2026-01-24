# Sample Auth API

Spring Boot 3 + Kotlin 기반의 JWT 인증 API 샘플 프로젝트입니다.

## 실행 방법

```bash
./gradlew bootRun
```

## 주요 엔드포인트

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/logout/all`
- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`
- `PUT /api/v1/users/me/password`
- `DELETE /api/v1/users/me`
- `GET /api/v1/users/me/devices`
- `DELETE /api/v1/users/me/devices/{deviceId}`
