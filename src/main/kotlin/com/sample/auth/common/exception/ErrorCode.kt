package com.sample.auth.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다"),
    AUTH_EXPIRED(HttpStatus.UNAUTHORIZED, "인증이 만료되었습니다. 다시 로그인해주세요"),
    AUTH_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다"),
    AUTH_INVALID_REFRESH(HttpStatus.UNAUTHORIZED, "유효하지 않은 갱신 토큰입니다"),
    AUTH_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "로그아웃된 세션입니다"),
    AUTH_DEVICE_MISMATCH(HttpStatus.UNAUTHORIZED, "다른 기기에서 발급된 인증 정보입니다"),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    AUTH_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_EMAIL_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    USER_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다"),
    USER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다"),
    USER_PASSWORD_SAME(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다"),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "정지된 계정입니다"),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다"),

    DEVICE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "디바이스 ID가 필요합니다"),
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 디바이스입니다"),
    DEVICE_CANNOT_LOGOUT(HttpStatus.BAD_REQUEST, "현재 디바이스는 이 방법으로 로그아웃할 수 없습니다"),

    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 일시적으로 사용할 수 없습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다"),
}
