# Spring Kotlin JWT 인증 API 샘플 프로젝트 기획서
## Mobile App Service 용

---

## 1. 프로젝트 개요

### 1.1 목적
Spring Boot 3.x + Kotlin 기반의 **모바일 앱 서비스**를 위한 JWT(Access Token/Refresh Token) 인증 시스템 구현

### 1.2 주요 기능
- 회원가입 / 로그인 / 로그아웃
- **Device 기반** Access Token(AT) / Refresh Token(RT) 관리
- Token Refresh 메커니즘
- Redis 기반 토큰 관리 (Device별 RT 저장, Blacklist)
- 다중 디바이스 로그인 지원 및 관리
- 특정 디바이스 강제 로그아웃

### 1.3 대상 클라이언트
- iOS App
- Android App
- (선택) Mobile Web

---

## 2. 기술 스택

### 2.1 Core
| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Kotlin | 1.9.x |
| Framework | Spring Boot | 3.3.x |
| Build Tool | Gradle (Kotlin DSL) | 8.x |
| JDK | OpenJDK | 21 |

### 2.2 Security & Authentication
| 구분 | 기술 | 용도 |
|------|------|------|
| Security | Spring Security | 인증/인가 프레임워크 |
| JWT | Nimbus JOSE+JWT | JWT 생성/검증 |
| Crypto | BouncyCastle | 암호화 라이브러리 |

### 2.3 Data Layer
| 구분 | 기술 | 용도 |
|------|------|------|
| ORM | Spring Data JPA | 데이터 접근 |
| Query | QueryDSL | 동적 쿼리 |
| Cache | Redis (Redisson) | 토큰 저장, 세션 관리 |
| DB | MySQL / H2 | 영구 저장소 |

### 2.4 Logging & Monitoring
| 구분 | 기술 | 용도 |
|------|------|------|
| Logging | Log4j2 | 로깅 프레임워크 |
| Log Format | JSON (ECS) | 구조화된 로그 |
| Log Shipping | GELF | Graylog 연동 |
| SQL Logging | P6Spy | SQL 로깅/분석 |

---

## 3. 시스템 아키텍처

### 3.1 전체 아키텍처
```
┌─────────────────────────────────────────────────────────────────┐
│                    Mobile App (iOS / Android)                    │
│                        deviceId: UUID                            │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway / Load Balancer                 │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Logging Filter (MDC)                    │  │
│  │    traceId, userId, deviceId, clientIp, userAgent         │  │
│  └───────────────────────────────────────────────────────────┘  │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐        │
│  │   Controller  │  │    Service    │  │  Repository   │        │
│  └───────────────┘  └───────────────┘  └───────────────┘        │
│          │                  │                  │                 │
│          ▼                  ▼                  ▼                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              Spring Security Filter Chain                  │  │
│  │  ┌─────────────────┐  ┌─────────────────────────────────┐ │  │
│  │  │ JwtAuthFilter   │  │ DeviceAuthenticationProvider    │ │  │
│  │  └─────────────────┘  └─────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
          │                       │                    │
          ▼                       ▼                    ▼
┌─────────────────┐      ┌─────────────────┐   ┌──────────────┐
│     MySQL       │      │     Redis       │   │   Graylog    │
│  (User Data)    │      │  (Token Store)  │   │   (Logs)     │
└─────────────────┘      └─────────────────┘   └──────────────┘
```

### 3.2 Device 기반 인증 Flow

#### 3.2.1 로그인 Flow (Device 등록)
```
┌────────┐     ┌────────────┐     ┌─────────────┐     ┌───────┐     ┌───────┐
│  App   │     │ Controller │     │ AuthService │     │ MySQL │     │ Redis │
└───┬────┘     └─────┬──────┘     └──────┬──────┘     └───┬───┘     └───┬───┘
    │                │                   │                │             │
    │ POST /api/v1/auth/login            │                │             │
    │ Headers:                           │                │             │
    │   X-Device-Id: {deviceId}          │                │             │
    │   X-Device-Name: iPhone 15 Pro     │                │             │
    │   X-App-Version: 1.0.0             │                │             │
    │   X-OS-Type: iOS                   │                │             │
    │   X-OS-Version: 17.2               │                │             │
    │ Body: {email, password}            │                │             │
    │───────────────>│                   │                │             │
    │                │  authenticate()   │                │             │
    │                │──────────────────>│                │             │
    │                │                   │  findByEmail() │             │
    │                │                   │───────────────>│             │
    │                │                   │   User Data    │             │
    │                │                   │<───────────────│             │
    │                │                   │                │             │
    │                │                   │ verify password│             │
    │                │                   │────────────────│             │
    │                │                   │                │             │
    │                │                   │ save/update    │             │
    │                │                   │ UserDevice     │             │
    │                │                   │───────────────>│             │
    │                │                   │                │             │
    │                │                   │ generate AT/RT │             │
    │                │                   │ (with deviceId)│             │
    │                │                   │────────────────│             │
    │                │                   │                │             │
    │                │                   │ store RT       │             │
    │                │                   │ key: RT:{userId}:{deviceId}  │
    │                │                   │───────────────────────────────>│
    │                │                   │                │             │
    │                │   TokenResponse   │                │             │
    │                │<──────────────────│                │             │
    │  {AT, RT, exp} │                   │                │             │
    │<───────────────│                   │                │             │
```

#### 3.2.2 API 호출 Flow (Device 검증 포함)
```
┌────────┐     ┌──────────────┐     ┌────────────┐     ┌───────┐
│  App   │     │JwtAuthFilter │     │ Controller │     │ Redis │
└───┬────┘     └──────┬───────┘     └─────┬──────┘     └───┬───┘
    │                 │                   │                │
    │ GET /api/v1/users/me               │                │
    │ Headers:                            │                │
    │   Authorization: Bearer {AT}        │                │
    │   X-Device-Id: {deviceId}           │                │
    │────────────────>│                   │                │
    │                 │                   │                │
    │                 │ 1. validate AT signature          │
    │                 │────────────────   │                │
    │                 │               │   │                │
    │                 │<───────────────   │                │
    │                 │                   │                │
    │                 │ 2. check deviceId match           │
    │                 │    (AT.deviceId == Header.deviceId)
    │                 │────────────────   │                │
    │                 │               │   │                │
    │                 │<───────────────   │                │
    │                 │                   │                │
    │                 │ 3. check blacklist                │
    │                 │───────────────────────────────────>│
    │                 │                   │    not found   │
    │                 │<───────────────────────────────────│
    │                 │                   │                │
    │                 │ set SecurityContext               │
    │                 │──────────────────>│                │
    │                 │                   │                │
    │   Response      │                   │                │
    │<────────────────────────────────────│                │
```

#### 3.2.3 Token Refresh Flow
```
┌────────┐     ┌────────────┐     ┌─────────────┐     ┌───────┐
│  App   │     │ Controller │     │ AuthService │     │ Redis │
└───┬────┘     └─────┬──────┘     └──────┬──────┘     └───┬───┘
    │                │                   │                │
    │ POST /api/v1/auth/refresh          │                │
    │ Headers:                           │                │
    │   X-Device-Id: {deviceId}          │                │
    │   X-OS-Type: iOS                   │                │
    │   X-OS-Version: 17.2               │                │
    │ Body: {refreshToken}               │                │
    │───────────────>│                   │                │
    │                │   refresh()       │                │
    │                │──────────────────>│                │
    │                │                   │                │
    │                │                   │ 1. validate RT signature
    │                │                   │────────────────│
    │                │                   │                │
    │                │                   │ 2. check RT.deviceId == Header.deviceId
    │                │                   │────────────────│
    │                │                   │                │
    │                │                   │ 3. get stored RT
    │                │                   │ key: RT:{userId}:{deviceId}
    │                │                   │───────────────>│
    │                │                   │   RT Data      │
    │                │                   │<───────────────│
    │                │                   │                │
    │                │                   │ 4. verify RT matches stored
    │                │                   │────────────────│
    │                │                   │                │
    │                │                   │ 5. generate new AT
    │                │                   │────────────────│
    │                │                   │                │
    │                │                   │ 6. (RTR) rotate RT & store
    │                │                   │───────────────>│
    │                │                   │                │
    │                │   TokenResponse   │                │
    │                │<──────────────────│                │
    │  {new AT, RT}  │                   │                │
    │<───────────────│                   │                │
```

---

## 4. API 명세

### 4.1 공통 Request Headers (모든 API)

| Header | Required | Description | Example |
|--------|----------|-------------|---------|
| X-Device-Id | ✅ | 디바이스 고유 식별자 (UUID) | `550e8400-e29b-41d4-a716-446655440000` |
| X-Device-Name | 로그인 시 | 디바이스 이름 | `iPhone 15 Pro` |
| X-App-Version | ✅ | 앱 버전 | `1.0.0` |
| X-OS-Type | ✅ | OS 종류 | `iOS` / `Android` |
| X-OS-Version | ✅ | OS 버전 | `17.2` |
| X-Request-Id | 선택 | 요청 추적 ID | `uuid` |
| Authorization | 인증 API | Bearer Token | `Bearer eyJ...` |

### 4.2 인증 API (Public)

#### 4.2.1 회원가입
```yaml
POST /api/v1/auth/signup
Content-Type: application/json
X-Device-Id: {deviceId}
X-App-Version: 1.0.0
X-OS-Type: iOS
X-OS-Version: 17.2

Request:
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "name": "홍길동",
  "phoneNumber": "01012345678",
  "marketingAgreed": true
}

Response (201 Created):
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "홍길동",
    "createdAt": "2025-01-24T10:00:00Z"
  },
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

#### 4.2.2 로그인
```yaml
POST /api/v1/auth/login
Content-Type: application/json
X-Device-Id: 550e8400-e29b-41d4-a716-446655440000
X-Device-Name: iPhone 15 Pro
X-App-Version: 1.0.0
X-OS-Type: iOS
X-OS-Version: 17.2

Request:
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

Response (200 OK):
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "refreshExpiresIn": 2592000,
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "name": "홍길동"
    }
  },
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

#### 4.2.3 토큰 갱신
```yaml
POST /api/v1/auth/refresh
Content-Type: application/json
X-Device-Id: 550e8400-e29b-41d4-a716-446655440000
X-App-Version: 1.0.0
X-OS-Type: iOS
X-OS-Version: 17.2

Request:
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIs..."
}

Response (200 OK):
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJSUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "refreshExpiresIn": 2592000
  },
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

#### 4.2.4 로그아웃 (현재 디바이스)
```yaml
POST /api/v1/auth/logout
Authorization: Bearer {accessToken}
X-Device-Id: 550e8400-e29b-41d4-a716-446655440000

Response (200 OK):
{
  "success": true,
  "message": "Successfully logged out",
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

#### 4.2.5 전체 디바이스 로그아웃
```yaml
POST /api/v1/auth/logout/all
Authorization: Bearer {accessToken}
X-Device-Id: 550e8400-e29b-41d4-a716-446655440000

Response (200 OK):
{
  "success": true,
  "message": "Successfully logged out from all devices",
  "data": {
    "loggedOutDevices": 3
  },
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

### 4.3 사용자 API (Protected)

#### 4.3.1 내 정보 조회
```yaml
GET /api/v1/users/me
Authorization: Bearer {accessToken}
X-Device-Id: {deviceId}

Response (200 OK):
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "홍길동",
    "phoneNumber": "01012345678",
    "profileImageUrl": "https://cdn.example.com/profiles/user123.jpg",
    "marketingAgreed": true,
    "createdAt": "2025-01-24T10:00:00Z",
    "updatedAt": "2025-01-24T10:00:00Z"
  },
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

#### 4.3.2 내 정보 수정
```yaml
PATCH /api/v1/users/me
Authorization: Bearer {accessToken}
X-Device-Id: {deviceId}
Content-Type: application/json

Request:
{
  "name": "홍길동",
  "phoneNumber": "01098765432"
}

Response (200 OK):
{
  "success": true,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "홍길동",
    "phoneNumber": "01098765432",
    "updatedAt": "2025-01-24T11:00:00Z"
  },
  "timestamp": "2025-01-24T11:00:00Z",
  "traceId": "abc123"
}
```

#### 4.3.3 비밀번호 변경
```yaml
PUT /api/v1/users/me/password
Authorization: Bearer {accessToken}
X-Device-Id: {deviceId}
Content-Type: application/json

Request:
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass456!"
}

Response (200 OK):
{
  "success": true,
  "message": "Password changed successfully. Please login again.",
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}

# 비밀번호 변경 시 모든 디바이스 로그아웃 처리
```

#### 4.3.4 회원 탈퇴
```yaml
DELETE /api/v1/users/me
Authorization: Bearer {accessToken}
X-Device-Id: {deviceId}
Content-Type: application/json

Request:
{
  "password": "CurrentPass123!",
  "reason": "서비스 이용 불편"  // optional
}

Response (200 OK):
{
  "success": true,
  "message": "Account deleted successfully",
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

### 4.4 디바이스 관리 API (Protected)

#### 4.4.1 로그인된 디바이스 목록 조회
```yaml
GET /api/v1/users/me/devices
Authorization: Bearer {accessToken}
X-Device-Id: {deviceId}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "deviceId": "550e8400-e29b-41d4-a716-446655440000",
      "deviceName": "iPhone 15 Pro",
      "osType": "iOS",
      "osVersion": "17.2",
      "appVersion": "1.0.0",
      "lastLoginAt": "2025-01-24T10:00:00Z",
      "lastAccessAt": "2025-01-24T14:30:00Z",
      "ipAddress": "192.168.1.100",
      "isCurrent": true
    },
    {
      "deviceId": "660e8400-e29b-41d4-a716-446655440001",
      "deviceName": "Galaxy S24",
      "osType": "Android",
      "osVersion": "14",
      "appVersion": "1.0.0",
      "lastLoginAt": "2025-01-23T09:00:00Z",
      "lastAccessAt": "2025-01-23T18:00:00Z",
      "ipAddress": "192.168.1.101",
      "isCurrent": false
    }
  ],
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

#### 4.4.2 특정 디바이스 로그아웃 (강제)
```yaml
DELETE /api/v1/users/me/devices/{deviceId}
Authorization: Bearer {accessToken}
X-Device-Id: {currentDeviceId}

Response (200 OK):
{
  "success": true,
  "message": "Device logged out successfully",
  "timestamp": "2025-01-24T10:00:00Z",
  "traceId": "abc123"
}
```

---

## 5. 데이터베이스 설계

### 5.1 ERD
```
┌─────────────────────────────────────────┐
│                  users                   │
├─────────────────────────────────────────┤
│ PK │ id              │ BINARY(16)       │  -- UUID v7
├────┼─────────────────┼──────────────────┤
│ UQ │ email           │ VARCHAR(255)     │
│    │ password        │ VARCHAR(255)     │  -- BCrypt
│    │ name            │ VARCHAR(100)     │
│    │ phone_number    │ VARCHAR(20)      │
│    │ profile_image_url│ VARCHAR(500)    │
│    │ status          │ VARCHAR(20)      │  -- ACTIVE, INACTIVE, WITHDRAWN
│    │ marketing_agreed│ BOOLEAN          │
│    │ last_login_at   │ DATETIME         │
│    │ created_at      │ DATETIME         │
│    │ updated_at      │ DATETIME         │
│    │ deleted_at      │ DATETIME         │  -- Soft delete
└────┴─────────────────┴──────────────────┘
                    │
                    │ 1:N
                    ▼
┌─────────────────────────────────────────┐
│              user_devices                │
├─────────────────────────────────────────┤
│ PK │ id              │ BIGINT AUTO_INC  │
├────┼─────────────────┼──────────────────┤
│ FK │ user_id         │ BINARY(16)       │
│ UQ │ device_id       │ VARCHAR(100)     │  -- Client generated UUID
│    │ device_name     │ VARCHAR(100)     │
│    │ os_type         │ VARCHAR(20)      │  -- iOS, Android
│    │ os_version      │ VARCHAR(20)      │
│    │ app_version     │ VARCHAR(20)      │
│    │ push_token      │ VARCHAR(500)     │  -- FCM/APNs token
│    │ last_login_at   │ DATETIME         │
│    │ last_login_ip   │ VARCHAR(45)      │
│    │ last_access_at  │ DATETIME         │
│    │ is_active       │ BOOLEAN          │
│    │ created_at      │ DATETIME         │
│    │ updated_at      │ DATETIME         │
└────┴─────────────────┴──────────────────┘
        │
        │ INDEX: (user_id, device_id) UNIQUE
        │ INDEX: (user_id, is_active)

┌─────────────────────────────────────────┐
│            login_histories               │
├─────────────────────────────────────────┤
│ PK │ id              │ BIGINT AUTO_INC  │
├────┼─────────────────┼──────────────────┤
│ FK │ user_id         │ BINARY(16)       │
│    │ device_id       │ VARCHAR(100)     │
│    │ ip_address      │ VARCHAR(45)      │
│    │ user_agent      │ VARCHAR(500)     │
│    │ os_type         │ VARCHAR(20)      │
│    │ app_version     │ VARCHAR(20)      │
│    │ login_at        │ DATETIME         │
│    │ login_type      │ VARCHAR(20)      │  -- EMAIL, SOCIAL_KAKAO, etc.
│    │ success         │ BOOLEAN          │
│    │ failure_reason  │ VARCHAR(100)     │
└────┴─────────────────┴──────────────────┘
        │
        │ INDEX: (user_id, login_at DESC)
        │ INDEX: (device_id, login_at DESC)
```

### 5.2 Redis 데이터 구조

#### Refresh Token 저장 (Device별)
```
Key: auth:rt:{userId}:{deviceId}
Value: {
  "tokenId": "jti-uuid",
  "userId": "user-uuid",
  "deviceId": "device-uuid",
  "deviceName": "iPhone 15 Pro",
  "osType": "iOS",
  "appVersion": "1.0.0",
  "ipAddress": "192.168.1.100",
  "issuedAt": 1706090400,
  "expiresAt": 1708682400
}
TTL: 30 days (2592000 seconds)
Type: String (JSON)
```

#### Access Token Blacklist
```
Key: auth:blacklist:{jti}
Value: {
  "userId": "user-uuid",
  "deviceId": "device-uuid",
  "reason": "logout",  // logout, password_change, force_logout
  "revokedAt": 1706090400
}
TTL: AT 남은 만료시간
Type: String (JSON)
```

#### User's Active Devices Set (빠른 조회용)
```
Key: auth:devices:{userId}
Value: Set of deviceIds
Type: Set
Operations: SADD, SREM, SMEMBERS
```

#### Rate Limiting
```
Key: auth:ratelimit:{endpoint}:{identifier}
     - login: auth:ratelimit:login:{ip}
     - refresh: auth:ratelimit:refresh:{userId}:{deviceId}
Value: count
TTL: 1 minute
Type: String (Counter)
```

---

## 6. 보안 설계

### 6.1 JWT 토큰 설계

#### Access Token (AT)
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "key-2025-01"
  },
  "payload": {
    "iss": "sample-app-auth",
    "sub": "550e8400-e29b-41d4-a716-446655440000",
    "aud": "sample-app-api",
    "iat": 1706090400,
    "exp": 1706092200,
    "jti": "at-unique-id",
    "type": "access",
    "deviceId": "device-uuid",
    "email": "user@example.com",
    "name": "홍길동"
  }
}
```
- **유효기간**: 30분
- **서명 알고리즘**: RS256
- **특징**: deviceId 포함으로 디바이스 바인딩

#### Refresh Token (RT)
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "key-2025-01"
  },
  "payload": {
    "iss": "sample-app-auth",
    "sub": "550e8400-e29b-41d4-a716-446655440000",
    "iat": 1706090400,
    "exp": 1708682400,
    "jti": "rt-unique-id",
    "type": "refresh",
    "deviceId": "device-uuid"
  }
}
```
- **유효기간**: 30일
- **서명 알고리즘**: RS256
- **특징**: 최소한의 정보만 포함

### 6.2 Device 검증 로직

```kotlin
// 1. AT의 deviceId와 Header의 deviceId 일치 확인
if (tokenDeviceId != headerDeviceId) {
    throw DeviceMismatchException("Device ID mismatch")
}

// 2. RT Refresh 시에도 동일하게 검증
if (refreshTokenDeviceId != headerDeviceId) {
    throw DeviceMismatchException("Refresh token device mismatch")
}

// 3. Redis에 저장된 RT와 비교
val storedRt = redis.get("auth:rt:${userId}:${deviceId}")
if (storedRt == null || storedRt.tokenId != refreshToken.jti) {
    throw InvalidRefreshTokenException("Refresh token not found or revoked")
}
```

### 6.3 보안 정책

#### 비밀번호 정책
- 최소 8자 이상
- 영문, 숫자, 특수문자 조합
- BCrypt 암호화 (Strength: 12)

#### Rate Limiting
| Endpoint | Limit | Window | Key |
|----------|-------|--------|-----|
| /auth/login | 5회 | 1분 | IP |
| /auth/signup | 3회 | 1분 | IP |
| /auth/refresh | 10회 | 1분 | userId:deviceId |
| 일반 API | 100회 | 1분 | userId |

#### Token 보안
- **Refresh Token Rotation (RTR)**: RT 사용 시 새 RT 발급
- **Device Binding**: 토큰과 디바이스 ID 바인딩
- **Blacklist**: 로그아웃/탈취 의심 시 즉시 무효화

---

## 7. 로깅 설계

### 7.1 로깅 전략

#### 로그 레벨 정책
| Level | 용도 | 예시 |
|-------|------|------|
| ERROR | 시스템 오류, 예외 | DB 연결 실패, 외부 API 오류 |
| WARN | 주의 필요한 상황 | Rate limit 초과, 유효하지 않은 토큰 |
| INFO | 중요 비즈니스 이벤트 | 로그인 성공/실패, 회원가입, 토큰 발급 |
| DEBUG | 개발/디버깅용 상세 정보 | 메서드 진입/종료, 파라미터 값 |
| TRACE | 매우 상세한 추적 정보 | SQL 쿼리, 외부 API 요청/응답 |

### 7.2 MDC (Mapped Diagnostic Context) 설계

```kotlin
// 모든 요청에 자동 설정되는 MDC 필드
MDC.put("traceId", traceId)           // 요청 추적 ID
MDC.put("spanId", spanId)             // 스팬 ID
MDC.put("userId", userId)             // 인증된 사용자 ID
MDC.put("deviceId", deviceId)         // 디바이스 ID
MDC.put("clientIp", clientIp)         // 클라이언트 IP
MDC.put("userAgent", userAgent)       // User-Agent
MDC.put("appVersion", appVersion)     // 앱 버전
MDC.put("osType", osType)             // OS 타입
MDC.put("requestUri", requestUri)     // 요청 URI
MDC.put("requestMethod", method)      // HTTP 메서드
```

### 7.3 Log4j2 설정

#### log4j2.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" monitorInterval="30">
    <Properties>
        <Property name="LOG_PATH">${sys:LOG_PATH:-./logs}</Property>
        <Property name="APP_NAME">${sys:APP_NAME:-sample-auth-api}</Property>
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - [%X{traceId}] [%X{userId}] [%X{deviceId}] %msg%n</Property>
    </Properties>

    <Appenders>
        <!-- Console Appender (개발용) -->
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>

        <!-- JSON Console Appender (운영용 - stdout to container log) -->
        <Console name="JsonConsole" target="SYSTEM_OUT">
            <JsonTemplateLayout eventTemplateUri="classpath:log4j2-ecs-layout.json"/>
        </Console>

        <!-- Rolling File Appender -->
        <RollingFile name="RollingFile"
                     fileName="${LOG_PATH}/${APP_NAME}.log"
                     filePattern="${LOG_PATH}/${APP_NAME}-%d{yyyy-MM-dd}-%i.log.gz">
            <JsonTemplateLayout eventTemplateUri="classpath:log4j2-ecs-layout.json"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="100MB"/>
            </Policies>
            <DefaultRolloverStrategy max="30">
                <Delete basePath="${LOG_PATH}" maxDepth="1">
                    <IfFileName glob="${APP_NAME}-*.log.gz"/>
                    <IfLastModified age="30d"/>
                </Delete>
            </DefaultRolloverStrategy>
        </RollingFile>

        <!-- GELF Appender (Graylog 연동) -->
        <GELF name="Graylog"
              host="${sys:GRAYLOG_HOST:-localhost}"
              port="${sys:GRAYLOG_PORT:-12201}"
              protocol="UDP"
              includeStackTrace="true"
              includeThreadContext="true">
            <KeyValuePair key="application" value="${APP_NAME}"/>
            <KeyValuePair key="environment" value="${sys:ENVIRONMENT:-local}"/>
        </GELF>

        <!-- Auth 이벤트 전용 파일 -->
        <RollingFile name="AuthEventFile"
                     fileName="${LOG_PATH}/auth-events.log"
                     filePattern="${LOG_PATH}/auth-events-%d{yyyy-MM-dd}-%i.log.gz">
            <JsonTemplateLayout eventTemplateUri="classpath:log4j2-ecs-layout.json"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="50MB"/>
            </Policies>
            <DefaultRolloverStrategy max="90"/>
        </RollingFile>

        <!-- 에러 전용 파일 -->
        <RollingFile name="ErrorFile"
                     fileName="${LOG_PATH}/error.log"
                     filePattern="${LOG_PATH}/error-%d{yyyy-MM-dd}-%i.log.gz">
            <JsonTemplateLayout eventTemplateUri="classpath:log4j2-ecs-layout.json"/>
            <ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="50MB"/>
            </Policies>
            <DefaultRolloverStrategy max="90"/>
        </RollingFile>
    </Appenders>

    <Loggers>
        <!-- Application Logger -->
        <Logger name="com.sample.auth" level="${sys:LOG_LEVEL:-INFO}" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFile"/>
            <AppenderRef ref="Graylog"/>
        </Logger>

        <!-- Auth 이벤트 전용 Logger -->
        <Logger name="com.sample.auth.event" level="INFO" additivity="false">
            <AppenderRef ref="AuthEventFile"/>
            <AppenderRef ref="Graylog"/>
        </Logger>

        <!-- Security Logger -->
        <Logger name="org.springframework.security" level="WARN" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFile"/>
        </Logger>

        <!-- SQL Logger (P6Spy) -->
        <Logger name="p6spy" level="${sys:SQL_LOG_LEVEL:-INFO}" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>

        <!-- Hibernate -->
        <Logger name="org.hibernate.SQL" level="DEBUG" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        <Logger name="org.hibernate.type.descriptor.sql" level="TRACE" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>

        <!-- Redis -->
        <Logger name="io.lettuce" level="WARN"/>
        <Logger name="org.redisson" level="WARN"/>

        <!-- Root Logger -->
        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="RollingFile"/>
            <AppenderRef ref="ErrorFile"/>
            <AppenderRef ref="Graylog"/>
        </Root>
    </Loggers>
</Configuration>
```

### 7.4 ECS (Elastic Common Schema) JSON Layout

#### log4j2-ecs-layout.json
```json
{
  "@timestamp": {
    "$resolver": "timestamp",
    "pattern": {
      "format": "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      "timeZone": "UTC"
    }
  },
  "ecs.version": "1.2.0",
  "log.level": {
    "$resolver": "level",
    "field": "name"
  },
  "log.logger": {
    "$resolver": "logger",
    "field": "name"
  },
  "message": {
    "$resolver": "message",
    "stringified": true
  },
  "process.thread.name": {
    "$resolver": "thread",
    "field": "name"
  },
  "error.type": {
    "$resolver": "exception",
    "field": "className"
  },
  "error.message": {
    "$resolver": "exception",
    "field": "message"
  },
  "error.stack_trace": {
    "$resolver": "exception",
    "field": "stackTrace",
    "stackTrace": {
      "stringified": true
    }
  },
  "trace.id": {
    "$resolver": "mdc",
    "key": "traceId"
  },
  "span.id": {
    "$resolver": "mdc",
    "key": "spanId"
  },
  "user.id": {
    "$resolver": "mdc",
    "key": "userId"
  },
  "device.id": {
    "$resolver": "mdc",
    "key": "deviceId"
  },
  "client.ip": {
    "$resolver": "mdc",
    "key": "clientIp"
  },
  "user_agent.original": {
    "$resolver": "mdc",
    "key": "userAgent"
  },
  "app.version": {
    "$resolver": "mdc",
    "key": "appVersion"
  },
  "os.type": {
    "$resolver": "mdc",
    "key": "osType"
  },
  "http.request.method": {
    "$resolver": "mdc",
    "key": "requestMethod"
  },
  "url.path": {
    "$resolver": "mdc",
    "key": "requestUri"
  },
  "service.name": "${sys:APP_NAME:-sample-auth-api}",
  "service.environment": "${sys:ENVIRONMENT:-local}"
}
```

### 7.5 P6Spy 설정 (SQL 로깅)

#### spy.properties
```properties
# P6Spy Configuration
driverlist=com.mysql.cj.jdbc.Driver
autoflush=false
dateformat=yyyy-MM-dd HH:mm:ss.SSS
appender=com.p6spy.engine.spy.appender.Slf4JLogger
logMessageFormat=com.sample.auth.infra.logging.P6SpyPrettySqlFormatter
excludecategories=info,debug,result,resultset
```

### 7.6 인증 이벤트 로거

#### AuthEventLogger.kt
```kotlin
@Component
class AuthEventLogger {

    private val logger = LoggerFactory.getLogger("com.sample.auth.event")

    fun logLoginSuccess(userId: String, deviceId: String, ipAddress: String) {
        enrichMdc(userId, deviceId, ipAddress)
        logger.info("LOGIN_SUCCESS - User logged in successfully")
    }

    fun logLoginFailure(email: String, deviceId: String, ipAddress: String, reason: String) {
        MDC.put("email", email)
        MDC.put("deviceId", deviceId)
        MDC.put("clientIp", ipAddress)
        MDC.put("failureReason", reason)
        logger.warn("LOGIN_FAILURE - Login attempt failed: {}", reason)
    }

    fun logLogout(userId: String, deviceId: String, logoutType: String) {
        enrichMdc(userId, deviceId, null)
        MDC.put("logoutType", logoutType)  // SELF, FORCE, ALL_DEVICES
        logger.info("LOGOUT - User logged out: {}", logoutType)
    }

    fun logTokenRefresh(userId: String, deviceId: String) {
        enrichMdc(userId, deviceId, null)
        logger.info("TOKEN_REFRESH - Access token refreshed")
    }

    fun logTokenRevoked(userId: String, deviceId: String, reason: String) {
        enrichMdc(userId, deviceId, null)
        MDC.put("revokeReason", reason)
        logger.warn("TOKEN_REVOKED - Token revoked: {}", reason)
    }

    fun logSuspiciousActivity(userId: String?, deviceId: String, ipAddress: String, activity: String) {
        enrichMdc(userId, deviceId, ipAddress)
        MDC.put("suspiciousActivity", activity)
        logger.warn("SUSPICIOUS_ACTIVITY - {}", activity)
    }

    fun logSignup(userId: String, email: String, deviceId: String) {
        enrichMdc(userId, deviceId, null)
        MDC.put("email", email)
        logger.info("SIGNUP - New user registered")
    }

    fun logPasswordChange(userId: String, deviceId: String) {
        enrichMdc(userId, deviceId, null)
        logger.info("PASSWORD_CHANGE - User changed password")
    }

    fun logAccountDeletion(userId: String, deviceId: String) {
        enrichMdc(userId, deviceId, null)
        logger.info("ACCOUNT_DELETION - User account deleted")
    }

    private fun enrichMdc(userId: String?, deviceId: String?, ipAddress: String?) {
        userId?.let { MDC.put("userId", it) }
        deviceId?.let { MDC.put("deviceId", it) }
        ipAddress?.let { MDC.put("clientIp", it) }
    }
}
```

### 7.7 환경별 로그 설정

| 환경 | Console | File | Graylog | SQL Log |
|------|---------|------|---------|---------|
| local | Pattern | - | - | Pretty |
| dev | JSON | JSON | ✅ | Pretty |
| staging | JSON | JSON | ✅ | Minimal |
| prod | JSON | JSON | ✅ | OFF |

---

## 8. 프로젝트 구조

```
src/main/kotlin/com/sample/auth/
├── SampleAuthApplication.kt
├── common/
│   ├── config/
│   │   ├── JpaConfig.kt
│   │   ├── QueryDslConfig.kt
│   │   ├── RedisConfig.kt
│   │   ├── RedissonConfig.kt
│   │   ├── SecurityConfig.kt
│   │   ├── SwaggerConfig.kt
│   │   └── WebConfig.kt
│   ├── constants/
│   │   └── AuthConstants.kt
│   ├── exception/
│   │   ├── ApiException.kt
│   │   ├── ErrorCode.kt
│   │   ├── ErrorResponse.kt
│   │   └── GlobalExceptionHandler.kt
│   ├── response/
│   │   ├── ApiResponse.kt
│   │   └── PageResponse.kt
│   └── util/
│       ├── Extensions.kt
│       └── IpUtils.kt
├── auth/
│   ├── controller/
│   │   └── AuthController.kt
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.kt
│   │   │   ├── SignupRequest.kt
│   │   │   ├── RefreshRequest.kt
│   │   │   └── LogoutRequest.kt
│   │   └── response/
│   │       ├── TokenResponse.kt
│   │       └── LoginResponse.kt
│   ├── service/
│   │   ├── AuthService.kt
│   │   └── AuthServiceImpl.kt
│   └── security/
│       ├── jwt/
│       │   ├── JwtTokenProvider.kt
│       │   ├── JwtAuthenticationFilter.kt
│       │   ├── JwtProperties.kt
│       │   └── TokenType.kt
│       ├── handler/
│       │   ├── CustomAuthenticationEntryPoint.kt
│       │   └── CustomAccessDeniedHandler.kt
│       ├── UserPrincipal.kt
│       └── CustomUserDetailsService.kt
├── user/
│   ├── controller/
│   │   ├── UserController.kt
│   │   └── DeviceController.kt
│   ├── dto/
│   │   ├── request/
│   │   │   ├── UpdateUserRequest.kt
│   │   │   ├── ChangePasswordRequest.kt
│   │   │   └── DeleteAccountRequest.kt
│   │   └── response/
│   │       ├── UserResponse.kt
│   │       └── DeviceResponse.kt
│   ├── entity/
│   │   ├── User.kt
│   │   ├── UserDevice.kt
│   │   ├── LoginHistory.kt
│   │   └── enums/
│   │       ├── UserStatus.kt
│   │       ├── OsType.kt
│   │       └── LoginType.kt
│   ├── repository/
│   │   ├── UserRepository.kt
│   │   ├── UserDeviceRepository.kt
│   │   ├── LoginHistoryRepository.kt
│   │   └── querydsl/
│   │       ├── UserRepositoryCustom.kt
│   │       └── UserRepositoryCustomImpl.kt
│   └── service/
│       ├── UserService.kt
│       ├── UserServiceImpl.kt
│       ├── DeviceService.kt
│       └── DeviceServiceImpl.kt
└── infra/
    ├── redis/
    │   ├── RedisTokenRepository.kt
    │   ├── RefreshTokenData.kt
    │   └── BlacklistData.kt
    ├── logging/
    │   ├── MdcLoggingFilter.kt
    │   ├── RequestResponseLoggingFilter.kt
    │   ├── AuthEventLogger.kt
    │   └── P6SpyPrettySqlFormatter.kt
    └── ratelimit/
        ├── RateLimitFilter.kt
        └── RateLimitProperties.kt

src/main/resources/
├── application.yml
├── application-local.yml
├── application-dev.yml
├── application-prod.yml
├── log4j2.xml
├── log4j2-ecs-layout.json
├── spy.properties
└── keys/
    ├── private.pem
    └── public.pem
```

---

## 9. 설정 파일

### 9.1 application.yml
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

  application:
    name: sample-auth-api

  main:
    banner-mode: off

  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null

server:
  port: 8080
  shutdown: graceful
  servlet:
    context-path: /
  tomcat:
    connection-timeout: 5s
    max-connections: 8192
    threads:
      max: 200
      min-spare: 20

---
# Local Profile
spring:
  config:
    activate:
      on-profile: local

  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100

  data:
    redis:
      host: localhost
      port: 6379

  h2:
    console:
      enabled: true
      path: /h2-console

jwt:
  issuer: sample-auth-api
  audience: sample-app
  access-token:
    expiration-seconds: 1800
  refresh-token:
    expiration-seconds: 2592000
  key-pair:
    private-key-path: classpath:keys/private.pem
    public-key-path: classpath:keys/public.pem

logging:
  level:
    root: INFO
    com.sample.auth: DEBUG
    org.springframework.security: DEBUG
    p6spy: DEBUG

decorator:
  datasource:
    p6spy:
      enable-logging: true

---
# Production Profile
spring:
  config:
    activate:
      on-profile: prod

  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&serverTimezone=Asia/Seoul
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      idle-timeout: 30000
      connection-timeout: 20000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        default_batch_fetch_size: 100
        generate_statistics: false

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      ssl:
        enabled: true

jwt:
  issuer: sample-auth-api
  audience: sample-app
  access-token:
    expiration-seconds: 1800
  refresh-token:
    expiration-seconds: 2592000
  key-pair:
    private-key-path: ${JWT_PRIVATE_KEY_PATH}
    public-key-path: ${JWT_PUBLIC_KEY_PATH}

logging:
  level:
    root: WARN
    com.sample.auth: INFO

decorator:
  datasource:
    p6spy:
      enable-logging: false
```

---

## 10. 에러 코드 정의

### 10.1 인증 관련 (AUTH)
| Code | HTTP | Message (KO) |
|------|------|--------------|
| AUTH_001 | 401 | 이메일 또는 비밀번호가 올바르지 않습니다 |
| AUTH_002 | 401 | 인증이 만료되었습니다. 다시 로그인해주세요 |
| AUTH_003 | 401 | 유효하지 않은 인증 정보입니다 |
| AUTH_004 | 401 | 세션이 만료되었습니다. 다시 로그인해주세요 |
| AUTH_005 | 401 | 유효하지 않은 갱신 토큰입니다 |
| AUTH_006 | 401 | 로그아웃된 세션입니다 |
| AUTH_007 | 401 | 다른 기기에서 발급된 인증 정보입니다 |
| AUTH_008 | 403 | 접근 권한이 없습니다 |
| AUTH_009 | 429 | 로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요 |

### 10.2 사용자 관련 (USER)
| Code | HTTP | Message (KO) |
|------|------|--------------|
| USER_001 | 404 | 사용자를 찾을 수 없습니다 |
| USER_002 | 409 | 이미 사용 중인 이메일입니다 |
| USER_003 | 400 | 비밀번호 형식이 올바르지 않습니다 |
| USER_004 | 400 | 현재 비밀번호가 일치하지 않습니다 |
| USER_005 | 400 | 새 비밀번호는 현재 비밀번호와 달라야 합니다 |
| USER_006 | 403 | 정지된 계정입니다 |
| USER_007 | 403 | 탈퇴한 계정입니다 |

### 10.3 디바이스 관련 (DEVICE)
| Code | HTTP | Message (KO) |
|------|------|--------------|
| DEVICE_001 | 400 | 디바이스 ID가 필요합니다 |
| DEVICE_002 | 404 | 등록되지 않은 디바이스입니다 |
| DEVICE_003 | 400 | 현재 디바이스는 이 방법으로 로그아웃할 수 없습니다 |

### 10.4 시스템 관련 (SYS)
| Code | HTTP | Message (KO) |
|------|------|--------------|
| SYS_001 | 500 | 서버 오류가 발생했습니다 |
| SYS_002 | 503 | 서비스를 일시적으로 사용할 수 없습니다 |
| SYS_003 | 400 | 잘못된 요청입니다 |
| SYS_004 | 400 | 입력값 검증에 실패했습니다 |

---

## 11. 개발 일정 (예상)

| Phase | Task | Duration |
|-------|------|----------|
| 1 | 프로젝트 초기 설정, Gradle, Log4j2 설정 | 0.5일 |
| 2 | Security Config, JWT Provider 구현 | 1일 |
| 3 | Entity, Repository 구현 | 0.5일 |
| 4 | Auth Service/Controller 구현 | 1일 |
| 5 | User/Device Service/Controller 구현 | 1일 |
| 6 | Redis Token 관리, Rate Limiting | 0.5일 |
| 7 | Exception Handler, Validation | 0.5일 |
| 8 | Logging Filter, MDC 설정 | 0.5일 |
| 9 | 테스트 코드 작성 | 1일 |
| 10 | API 문서화 (Swagger) | 0.5일 |
| **Total** | | **7일** |

---

## 12. 테스트 체크리스트

### 12.1 인증 Flow
- [ ] 회원가입 → 로그인 → API 호출 → 토큰 갱신 → 로그아웃
- [ ] 만료된 AT로 API 호출 → 401 응답 확인
- [ ] 만료된 RT로 갱신 요청 → 401 응답 확인
- [ ] 로그아웃된 토큰으로 API 호출 → 401 응답 확인

### 12.2 Device 관리
- [ ] 다른 디바이스에서 동시 로그인
- [ ] 디바이스 목록 조회
- [ ] 특정 디바이스 강제 로그아웃
- [ ] 전체 디바이스 로그아웃
- [ ] Device ID 불일치 시 401 응답

### 12.3 보안
- [ ] Rate Limiting 동작 확인
- [ ] 비밀번호 변경 후 모든 세션 무효화
- [ ] RTR (Refresh Token Rotation) 동작 확인

### 12.4 로깅
- [ ] MDC 필드 정상 출력 확인
- [ ] 인증 이벤트 로그 기록 확인
- [ ] 에러 발생 시 스택트레이스 포함 확인
- [ ] JSON 포맷 로그 파싱 가능 여부

---

## 부록 A: Header 규격 상세

```
# Required Headers (모든 요청)
X-Device-Id: string (UUID v4)
  - 앱 최초 실행 시 생성, 앱 삭제 전까지 유지
  - iOS: identifierForVendor 또는 자체 생성 UUID
  - Android: ANDROID_ID 또는 자체 생성 UUID

X-App-Version: string (semver)
  - 예: "1.0.0", "2.1.3"

X-OS-Type: string (enum)
  - 허용값: "iOS", "Android"

X-OS-Version: string
  - iOS 예: "17.2", "16.5"
  - Android 예: "14", "13"

# Optional Headers
X-Device-Name: string (로그인 시 권장)
  - 예: "iPhone 15 Pro", "Galaxy S24 Ultra"

X-Request-Id: string (UUID)
  - 클라이언트에서 생성한 요청 추적 ID
  - 없으면 서버에서 자동 생성

X-Timezone: string
  - 예: "Asia/Seoul", "UTC"
```

---

## 부록 B: 의존성 버전 참고

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| Spring Boot | 3.3.x | 프레임워크 |
| Nimbus JOSE+JWT | 9.37.3 | JWT 처리 |
| Redisson | 3.52.0 | Redis 클라이언트 |
| QueryDSL | 5.x (jakarta) | 동적 쿼리 |
| BouncyCastle | bcprov-jdk18on 1.82 | 암호화 |
| Log4j2 GELF | 1.3.1 | Graylog 연동 |
| P6Spy | 1.12.0 | SQL 로깅 |
| UUID Creator | 6.1.1 | UUID 생성 |
| SpringDoc OpenAPI | 2.8.13 | API 문서화 |
